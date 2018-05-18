package ai.nodesense.example

import akka.actor.{Actor, ActorRef, ActorLogging, Props, ActorSystem, PoisonPill}
import akka.pattern._
import scala.concurrent.duration._
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global;


import scala.util.Random

case class Email(id: Int, to: String, subject: String, content: String)
case class EmailResponse(id: Int, status: Int);

case class SMS(to: Long, msg: String);
case class Document(title: String, document: String)

// For Ask pattern

case object AskInkLevel;
case class InkLevelResponse(yellow: Int, cyan: Int, magenta: Int);



case class Maintenance();

case class StopActor();

object NextID {
  val random = new Random();

  def getId = random.nextInt()
}

class EmailActor extends Actor with ActorLogging {
  def receive = {
    case  email:

      Email => println( s"Email message got $email.id ", email.subject)
      sender() ! EmailResponse(email.id, 0)

      println("*****", akka.serialization.Serialization.serializedActorPath(self))

    case StopActor() => context.stop(self)
    case _ => println("Unknown message");
  }
}


class PrinterActor(emailActor: ActorRef) extends Actor with ActorLogging {
  val random = new Random();



  def receive = {
    case document: Document => println("Printer doc ", document)

    case Maintenance() =>
      println("Sending message to email actor through route/path")
      context.actorSelection("../emailActor") ! Email(NextID.getId, "m@ex.com", "P1 from path ", "Good")

    case EmailResponse(id, status) => println(s"Printer email response $id, $status")

    case AskInkLevel => {
      println("Ask Ink Level")
      sender !  InkLevelResponse(random.nextInt(100), random.nextInt(100), random.nextInt(100))
    }

    case _ => println("Printer")
  }

  import scala.concurrent.duration.Duration
  import java.util.concurrent.TimeUnit

  context.system.scheduler.schedule(10 seconds, 30 seconds, self, Maintenance())
}

object PrinterActor {
  def props(emailActor: ActorRef) = Props(new PrinterActor(emailActor))
}

object  ActorsApp extends App  {
  val system:ActorSystem = ActorSystem("actors")

  // create new child actor, system is parent, emailActor is child actor
  val emailActor: ActorRef = system.actorOf(Props[EmailActor], "emailActor")

  val printerActor: ActorRef = system.actorOf(PrinterActor.props(emailActor), "printerActor")

  emailActor ! Email(NextID.getId, "ex@example.com", "Printer Down 1", "Empty tank")
  emailActor ! Email(NextID.getId, "ex@example.com", "Printer Down 2", "Empty tank")


  // system.actorSelection("/users/emailActor") ! Email(NextID.getId, "ex@example.com", "Using path ", "Route path")

  //Thread.sleep(20);

  // stop the actor, next messages in queue not processed
  // system.stop(emailActor)

  // added to queue as other messages, other messages in queue are processed
  // after poison pill, no other messages received

  //emailActor ! PoisonPill

  // emailActor ! StopActor()


  emailActor ! "Hello"

  printerActor ! Document("New Policy", "Police Number...")
  printerActor ! Maintenance()


  implicit val timeout = Timeout(5 seconds)

  val future = printerActor ? AskInkLevel


  val result = Await.result(future, timeout.duration).asInstanceOf[InkLevelResponse]
  println("Ink Level ", result)


}
