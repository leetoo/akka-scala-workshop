package ai.nodesense.example.tryout


import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn


object ActorPerRequest extends App {

  object RequestHandler {
    case class Handle(complete: String => Unit)
  }

  import ActorPerRequest.RequestHandler._

  class RequestHandler extends Actor {
    println("New actor created ")

    def receive = {
      case Handle(complete) =>
        complete("ok")
        context.stop(self)
    }
  }

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val route =
    pathEndOrSingleSlash {
      get {
        completeWith(implicitly[ToResponseMarshaller[String]]) { f =>
          system.actorOf(Props[RequestHandler]) ! RequestHandler.Handle(f)
        }
      }
    }

  Http().bindAndHandle(route, "localhost", 8080)

  println("ENTER to terminate")
  StdIn.readLine()
  system.terminate()
}
