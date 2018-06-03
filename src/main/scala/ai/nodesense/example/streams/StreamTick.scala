package ai.nodesense.example.streams


/**
  * Created by Gopalakrishnan
  */

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.duration._
import scala.util.Random

object Tick {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("source")
    implicit val materializer = ActorMaterializer()

    val r = new Random();
    def next: Int = {
      println("Next called");
      r.nextInt(10)
    }

    val source: Source[Int, Cancellable] = Source.tick(0.seconds, 1.seconds, next)
    source.runForeach(println)

  }
}