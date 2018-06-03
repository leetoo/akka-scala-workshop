package ai.nodesense.example.mailbox

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.dispatch._
import com.typesafe.config.{Config, ConfigFactory}
import java.util.concurrent.ConcurrentLinkedQueue

import scala.Option

trait MyUnboundedMessageQueueSemantics


object MyUnboundedMailbox {

  // This is the MessageQueue implementation
  class MyMessageQueue extends MessageQueue
    with MyUnboundedMessageQueueSemantics {

    private final val queue = new ConcurrentLinkedQueue[Envelope]()

    // these should be implemented; queue used as example
    def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
      // insert statement
      queue.offer(handle)
    }


    def dequeue(): Envelope = queue.poll()
    def numberOfMessages: Int = queue.size
    def hasMessages: Boolean = !queue.isEmpty
    def cleanUp(owner: ActorRef, deadLetters: MessageQueue) {
      while (hasMessages) {
        deadLetters.enqueue(owner, dequeue())
      }
    }
  }
}

// This is the Mailbox implementation
class MyUnboundedMailbox extends MailboxType
  with ProducesMessageQueue[MyUnboundedMailbox.MyMessageQueue] {

  println("Custom mail box created");
  import MyUnboundedMailbox._

  // This constructor signature must exist, it will be called by Akka
  def this(settings: ActorSystem.Settings, config: Config) = {
    // put your initialization code here
    this()
  }

  // The create method is called to create the MessageQueue
  final override def create(
                             owner:  Option[ActorRef],
                             system: Option[ActorSystem]): MessageQueue =
    new MyMessageQueue()
}



  object CustomMailBoxApp extends  App {

  import akka.actor.Actor

  class MailboxActor extends Actor with  RequiresMessageQueue[MyUnboundedMessageQueueSemantics] {

    def receive = {
      case msg =>
        println(s" Received   ${msg}")
    }
  }

  val config = ConfigFactory.parseString(
    """
      |custom-dispatcher {
      |  mailbox-requirement =
      |  "ai.nodesense.example.mailbox.MyUnboundedMessageQueueSemantics"
      |}
      |
      |akka.actor.mailbox.requirements {
      |  "ai.nodesense.example.mailbox.MyUnboundedMessageQueueSemantics" =
      |  custom-dispatcher-mailbox
      |}
      |
      |custom-dispatcher-mailbox {
      |  mailbox-type = "ai.nodesense.example.mailbox.MyUnboundedMailbox"
      |}
    """.stripMargin)

  val system = ActorSystem("pririty-system", config)

  val mailboxActor = system.actorOf(Props[MailboxActor])



    mailboxActor ! 'lowpriority
  mailboxActor ! 'lowpriority
  mailboxActor ! 'highpriority
  mailboxActor ! 'pigdog
  mailboxActor ! 'pigdog2
  mailboxActor ! 'pigdog3
  mailboxActor ! 'highpriority
  mailboxActor ! PoisonPill

}