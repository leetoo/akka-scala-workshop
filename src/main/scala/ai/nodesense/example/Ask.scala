//package ai.nodesense.example
//
////import ai.nodesense.example.ActorsApp.{emailActor, printerActor, system}
//import akka.actor.{ActorRef, ActorSystem}
//import akka.util.Timeout
//import scala.concurrent.ExecutionContext.Implicits.global;
//
//import scala.concurrent.{Await, Future}
//import akka.pattern._
//import scala.concurrent.duration._
//import akka.pattern.ask
//
//
//object Ask extends  App {
//  val system:ActorSystem = ActorSystem("actors")
//
//  val printerActor: ActorRef = system.actorOf(PrinterActor.props(mailActor), "printerActor")
//
//  implicit val timeout = Timeout(5 seconds)
//
//  val future = printerActor ? AskInkLevel
//
//  val result = Await.result(future, timeout.duration).asInstanceOf[InkLevelResponse]
//  println("Ink Level ", result)
//
//  val future2: Future[InkLevelResponse] = ask(printerActor, AskInkLevel).mapTo[InkLevelResponse]
//  val result2 = Await.result(future2, 1 second)
//  println("Ink Level 3 ", result2)
//
//  val future3: Future[InkLevelResponse] = printerActor.ask(AskInkLevel).mapTo[InkLevelResponse]
//  val result3 = Await.result(future3, 1 second)
//  println("Ink Level 3 ", result3)
//
//  val future4 = printerActor ? AskInkLevel
//
//  future4.foreach(r => println("INK 4", r));
//}
