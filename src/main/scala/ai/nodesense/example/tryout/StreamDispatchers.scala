package ai.nodesense.example.tryout


import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory

object StreamDispatchers extends App {

  implicit val system = ActorSystem("dispatchers", ConfigFactory.parseString(
    """
      another {
        type = Dispatcher
        executor = "thread-pool-executor"
        thread-pool-executor {
          fixed-pool-size = 1
        }
      }
      yet-another {
        type = Dispatcher
        executor = "thread-pool-executor"
        thread-pool-executor {
          fixed-pool-size = 1
        }
      }
    """))
  implicit val materializer = ActorMaterializer()

  val source = Source(0 to 200)
    .map { n => println("expected default: " + Thread.currentThread().getName); n }
    .map { n => println("expected another: " + Thread.currentThread().getName); n }
    .addAttributes(ActorAttributes.dispatcher("another"))
    .map { n => println("expected yet-another: " + Thread.currentThread().getName); n }
    .addAttributes(ActorAttributes.dispatcher("yet-another"))
    .runForeach(n => println("expected default: " + Thread.currentThread().getName))
}