package ai.nodesense.example.tryout

import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.pattern.AskTimeoutException
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

object AskWithRetry extends App {

  implicit val system = ActorSystem("test")
  implicit val timeout: Timeout = 10 millis
  import system.dispatcher

  retry(system.deadLetters, "wat", 10, 0)

  println("Enter to exit")
  StdIn.readLine()
  system.terminate()


  def retry(actorRef: ActorRef, message: Any, maxAttempts: Int, attempt: Int): Future[Any] = {
    println(s"Retrying attempt $attempt")
    val future = (actorRef ? message) recover {
      case e: AskTimeoutException =>
        if (attempt <= maxAttempts) retry(actorRef, message, maxAttempts, attempt + 1)
        else None // Return default result according to your requirement, if actor is non-reachable.
    }
    future
  }
}