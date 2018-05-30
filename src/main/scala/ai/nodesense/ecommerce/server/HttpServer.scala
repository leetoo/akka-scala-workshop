package ai.nodesense.ecommerce.server

//#quick-start-server
import ai.nodesense.ecommerce.actors.{OrderActor, ProductActor, UserRegistryActor}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
//import example.actors.ActorsApp.system
//import example.actors.{EmailActor, PrinterActor}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration._
import scala.concurrent.Future
import  ai.nodesense.ecommerce.db._

// Krish
import scala.concurrent.ExecutionContext.Implicits.global

//#main-class
object HttpServer extends App with Routes with Config {


  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("actors")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  //akka://actors/users/userRegistryActor
  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

  val productActor: ActorRef = system.actorOf(ProductActor.props, "productActor")

  val orderActor: ActorRef = system.actorOf(OrderActor.props, "orderActor")

  //val emailActor: ActorRef = system.actorOf(Props[EmailActor], "emailActor")

  //val printerActor: ActorRef = system.actorOf(PrinterActor.props(emailActor), "printerActor")


  //#main-class
  // from the UserRoutes trait
  //lazy val routes: Route = userRoutes
  // lazy val routes: Route = productRoutes
  lazy val routes = userRoutes ~ productRoutes ~ orderRoutes
  //#main-class

  //#http-server
  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  system.registerOnTermination(() => session.close())


  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
