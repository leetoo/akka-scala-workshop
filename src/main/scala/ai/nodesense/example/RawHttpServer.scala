package ai.nodesense.example


import akka.actor.{ ActorRef, ActorSystem }
import akka.actor.{Actor, ActorLogging, Props}

import akka.event.Logging

import scala.concurrent.Await


import scala.concurrent.duration._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import akka.http.scaladsl.server.directives._
import ContentTypeResolver.Default


import akka.http.scaladsl.model.{ HttpEntity,
  ContentTypes,
  HttpRequest,
  HttpResponse,
  Uri
}

import akka.http.scaladsl.model.HttpMethods.{
  GET, PUT, POST, DELETE, OPTIONS
}

import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import akka.stream.ActorMaterializer




object RawHttpServer  extends App   {

  implicit val system: ActorSystem = ActorSystem("actors")
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  // low level model

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        "<html><body>Hello world!</body></html>"))

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sys.error("BOOM!")

    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }


  val fileRoutes = path("logs" / Segment) { name =>
    println("Log path is", name)
    getFromFile(s"$name.log") // uses implicit ContentTypeResolver
  }

  var dirRoutes = pathPrefix("data") {

    getFromDirectory("/Users/krish/workshops/akka-scala-workshop/data")
  }

   // Low Level API Example
  Http().bindAndHandleSync(requestHandler, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
