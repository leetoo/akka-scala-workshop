package ai.nodesense.example.streams

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

import scala.io.StdIn
import scala.util.Random

object StreamPartitionApp  extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()

  case class Apple(bad: Boolean)

  val badApples = Sink.foreach[Apple](apple => println("bad apple"))
  val goodApples = Sink.foreach[Apple](apple => println("good apple"))
  val apples = Source(Vector.fill(10){ println("Generating");Apple(Random.nextBoolean()) })


  RunnableGraph.fromGraph(GraphDSL.create(){ implicit b =>
    import GraphDSL.Implicits._

    // The lambda is "apple => port number"
    val partition = b.add(Partition[Apple](2, apple => if (apple.bad) 1 else 0))

    apples ~> partition.in

    partition.out(0) ~> goodApples
    partition.out(1) ~> badApples

    ClosedShape
  }).run()


  println("ENTER to terminate")
  StdIn.readLine()
  system.terminate()

}
