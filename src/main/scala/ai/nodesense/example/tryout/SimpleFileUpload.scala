package ai.nodesense.example.tryout

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.io.StdIn

object SimpleFileUpload {
//
//  def main(args: Array[String]): Unit = {
//    implicit val system = ActorSystem("my-system")
//    implicit val materializer = ActorMaterializer()
//    import system.dispatcher
//
//    val route =
//      path("upload") {
//        post {
//          extractRequest { request =>
//            val file = File.createTempFile("tmp", "dat")
//            val futureDone = request.entity.dataBytes
//              .runWith(Sink.file(file))
//
//            onComplete(futureDone) { _ =>
//              complete("ok: " + file.getAbsolutePath)
//            }
//          }
//        }
//      }
//
//    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
//
//    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
//    StdIn.readLine()
//
//    import system.dispatcher
//    // for the future transformations
//    bindingFuture
//      .flatMap(_.unbind()) // trigger unbinding from the port
//      .onComplete(_ ⇒ system.shutdown()) // and shutdown when done
//  }
}