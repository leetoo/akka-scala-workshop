package ai.nodesense.example.tryout

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

//
//object StreamNumbers extends App {
//  implicit val system = ActorSystem("my-system")
//  implicit val materializer = ActorMaterializer()
//
//  implicit val toResponseMarshaller: ToResponseMarshaller[Source[Int, Any]] =
//    Marshaller.opaque { items =>
//      val data = items.map(item => ChunkStreamPart(item.toString + "\n"))
//      //HttpResponse(entity = HttpEntity.Chunked(MediaTypes.`text/plain`, data))
//      HttpResponse()
//    }
//
//  def newDataStream(): Stream[Int] = Stream.from(1)
//
//  val route =
//    path("numbers") {
//      get {
//        complete {
//          Source(() => newDataStream().toIterator)
//        }
//      }
//    }
//
//  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
//
//  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
//  Console.readLine()
//
//  import system.dispatcher // for the future transformations
//  bindingFuture
//    .flatMap(_.unbind()) // trigger unbinding from the port
//    .onComplete(_ ⇒ system.terminate()) // and shutdown when done
//
//}