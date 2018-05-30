package ai.nodesense.example.tryout

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object SimpleClient {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val http = Http()
    val response: Future[HttpResponse] =
      http.singleRequest(HttpRequest(HttpMethods.GET, Uri("https://jsonplaceholder.typicode.com/posts/1")))

    val result = response.map {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        Unmarshal(entity).to[String]

      case x => s"Unexpected status code ${x.status}"

    }

    println(Await.result(result, 10.seconds))

    http.shutdownAllConnectionPools()
    system.terminate()
  }

}