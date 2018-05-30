package ai.nodesense.example.tryout

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Rejection, RejectionHandler}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

// Seems not working, need to fix the flow

object RateLimit {

  object SlowActor {
    case object Ping
    case object Pong
  }
  class SlowActor extends Actor {
    import SlowActor._
    import context.dispatcher

    def receive = {
      case Ping =>
        // simulate something taking time to respond
        context.system.scheduler.scheduleOnce(10.seconds, sender(), Pong)
    }
  }

  case class PathBusyRejection(path: Uri.Path, max: Int) extends Rejection

  class Limiter(max: Int) {

    // needs to be a thread safe counter since there can be concurrent requests
    val concurrentRequests = new AtomicInteger(0)

    val limitConcurrentRequests: Directive0 =
      extractRequest.flatMap { request =>
        if (concurrentRequests.incrementAndGet() > max) {
          // we need to decrease it again, and then reject the request
          // this means you can use a rejection handler somwhere else, for
          // example around the entire Route turning all such rejections
          // to the same kind of actual HTTP response there
          concurrentRequests.decrementAndGet()
          reject(PathBusyRejection(request.uri.path, max))
        } else {
          mapResponse { response =>
            concurrentRequests.decrementAndGet()
            response
          }
        }

      }
  }


  def main(args: Array[String]): Unit = {
    // sample usage
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val slowActor = system.actorOf(Props[SlowActor])

    val rejectionHandler = RejectionHandler.newBuilder()
      .handle {
        case PathBusyRejection(path, max) =>
          complete((StatusCodes.EnhanceYourCalm, s"Max concurrent requests for $path reached, please try again later"))
      }.result()

    // needs to be created outside of the route tree or else
    // you get separate instances rather than sharing one
    val limiter = new Limiter(max = 2)

    val route =
      handleRejections(rejectionHandler) {
        path("max-2") {
          limiter.limitConcurrentRequests {
            implicit val timeout: Timeout = 20.seconds
            onSuccess(slowActor ? SlowActor.Ping) { _ =>
              complete("Done!")
            }
          }
        }
      }

    import system.dispatcher
    Http().bindAndHandle(route, "127.0.0.1", 8080).onComplete {
      case Success(_) => println("Listening for requests, call http://127.0.0.1:8080/max-2 to try out")
      case Failure(ex) =>
        println("Failed to bind to 127.0.0.8080")
        ex.printStackTrace()
    }

  }

}