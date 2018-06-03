package ai.nodesense.example.mailbox

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.dispatch.PriorityGenerator
import akka.dispatch.UnboundedPriorityMailbox
import com.typesafe.config.{Config, ConfigFactory}

// We inherit, in this case, from UnboundedPriorityMailbox
// and seed it with the priority generator
case class Message(msg: String, priority: Int)

class MyPrioMailbox(settings: ActorSystem.Settings, config: Config)
                                extends UnboundedPriorityMailbox(
  // Create a new PriorityGenerator, lower prio means more important

  PriorityGenerator {
    // 'highpriority messages should be treated first if possible
    case Message(_, priority) if priority < 5   => 0 // High

    case Message(_, priority) if priority < 10   => 1 //  Medium

    case Message(_, priority) if priority >= 10   => 2 //  Low

    case 'lowpriority  ⇒ 2

    // PoisonPill when no other left
    case PoisonPill    ⇒ 3

    // We default to 1, which is in between high and low
    case otherwise     ⇒ 1
  })


object MyPrioMailbox extends  App {

  import akka.actor.Actor

  class MailboxActor extends Actor {

    def receive = {

      case msg =>
        println(s" Received   ${msg}")


    }
  }


  val config = ConfigFactory.parseString(
    """
      |prio-dispatcher {
      |  mailbox-type = "ai.nodesense.example.mailbox.MyPrioMailbox"
      |}
    """.stripMargin)


  val system = ActorSystem("pririty-system", config)

  //val mailboxActor = system.actorOf(Props[MailboxActor])

  val mailboxActor = system.actorOf(Props[MailboxActor]
                              .withDispatcher("prio-dispatcher"))


  mailboxActor ! Message("MSG 20", 20)
  mailboxActor ! Message("MSG 1", 1)
  mailboxActor ! Message("MSG 2", 2)
  mailboxActor ! 'lowpriority
  mailboxActor ! 'lowpriority
  mailboxActor ! 'highpriority
  mailboxActor ! 'pigdog
  mailboxActor ! 'pigdog2
  mailboxActor ! 'pigdog3
  mailboxActor ! 'highpriority
  mailboxActor ! PoisonPill

}