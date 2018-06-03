package ai.nodesense.example.dispatchers

import akka.actor.{ActorRef, ActorSystem, Actor, Props}
import com.typesafe.config.ConfigFactory

object  DispatcherConfig {

  def loadConfig() = ConfigFactory.parseString(
    """
      |      custom-dispatcher {
      |        type = Dispatcher
      |        executor = "thread-pool-executor"
      |        thread-pool-executor {
      |          core-pool-size-min = 4
      |          core-pool-size-factor = 2.0
      |          core-pool-size-max = 8
      |        }
      |        throughput = 10
      |        mailbox-capacity = -1
      |        mailbox-type = ""
      |      }
      |
      |      fork-join-dispatcher {
      |  # Dispatcher is the name of the event-based dispatcher
      |  type = Dispatcher
      |  # What kind of ExecutionService to use
      |  executor = "fork-join-executor"
      |  # Configuration for the fork join pool
      |  fork-join-executor {
      |    # Min number of threads to cap factor-based parallelism number to
      |    parallelism-min = 2
      |    # Parallelism (threads) ... ceil(available processors * factor)
      |    parallelism-factor = 2.0
      |    # Max number of threads to cap factor-based parallelism number to
      |    parallelism-max = 10
      |  }
      |  # Throughput defines the maximum number of messages to be
      |  # processed per actor before the thread jumps to the next actor.
      |  # Set to 1 for as fair as possible.
      |  throughput = 100
      |}
      |
      |thread-pool-dispatcher {
      |  type = Dispatcher
      |  executor = "thread-pool-executor"
      |  thread-pool-executor {
      |   fixed-pool-size = 32
      |   throughput = 1
      |  }
      |}

    """.stripMargin)
}


class Worker extends Actor {
  import Worker._

  def receive = {
    case msg: Work =>
      val (thread) = (Thread.currentThread().getId() )
      println(s"Work $msg on thread $thread" )
      Thread.sleep(10)
  }
}

object Worker {
  def props(): Props = Props(new Worker())

  case class Work(message: String)
}

object CustomizedDispatchers extends App {
  import Worker._


  def runWorks(worker: ActorRef) = {

    for ( i <- 1 to 100) {
      worker ! Work(s"Work $i")
    }

  }

  def defaultDispatcher() = {
    val config = DispatcherConfig.loadConfig()

    val system: ActorSystem = ActorSystem.create("system", config)
    val worker: ActorRef = system.actorOf(Worker.props())
    runWorks(worker)
  }


  def customDispatcher() = {
    val config = DispatcherConfig.loadConfig()

    val system: ActorSystem = ActorSystem.create("system", config)
    val worker: ActorRef = system.actorOf(Worker.props().withDispatcher("custom-dispatcher"))
    runWorks(worker)
  }



  def customForkExecutorDispatcher() = {
    val config = DispatcherConfig.loadConfig()

    val system: ActorSystem = ActorSystem.create("system", config)
    val worker: ActorRef = system.actorOf(Worker.props().withDispatcher("fork-join-dispatcher"))
    runWorks(worker)
  }


  def threadPoolDispatcher() = {
    val config = DispatcherConfig.loadConfig()

    val system: ActorSystem = ActorSystem.create("system", config)
    val worker: ActorRef = system.actorOf(Worker.props().withDispatcher("thread-pool-dispatcher"))
    runWorks(worker)
  }


  //defaultDispatcher()
 // customDispatcher()
  //customForkExecutorDispatcher()
  threadPoolDispatcher()
}
