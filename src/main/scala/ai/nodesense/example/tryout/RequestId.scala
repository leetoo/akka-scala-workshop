package ai.nodesense.example.tryout

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server._

import scala.io.StdIn
import scala.util.Try

// CRASHES

//Example showing how to ensure a unique id for each request, and how to log that from the Akka HTTP server DSL

// custom header for request ids
object IdHeader extends ModeledCustomHeaderCompanion[IdHeader] {
  override def name: String = "X-Request-Id"
  override def parse(value: String): Try[IdHeader] = Try(IdHeader(value))
}
final case class IdHeader(id: String) extends ModeledCustomHeader[IdHeader] {
  override val renderInRequests = false
  override val renderInResponses = false
  override val companion = IdHeader
  override def value(): String = id
}

object RequestId extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  import Directives._

  def generateUniqueId = UUID.randomUUID().toString

  // a couple of shortcut directives
  val ensureRequestId: Directive0 = mapRequest { req: HttpRequest =>
    if (req.header[IdHeader].isDefined) req
    else req.addHeader(IdHeader(generateUniqueId))
  }

  val requestId: Directive1[IdHeader] = headerValueByType[IdHeader]()

  // will be logged to the actor system logger so the logger used needs to be configured to see this
  val logReqWithId = logRequest { req: HttpRequest =>
    s"${req.header[IdHeader].get} ${req.method} ${req.uri}"
  }


  // then wrap the routes with the directive making sure you always have
  // an id
  val route = ensureRequestId {
    logReqWithId {
      get {
        requestId { id =>
          complete {
            s"got id: ${id}"
          }
        }
      }
    }
  }

  Http().bindAndHandle(route, "localhost", 8080)


  println("Listening to http://localhost:8080, [ENTER] to terminate")
  StdIn.readLine()
  system.terminate()

}