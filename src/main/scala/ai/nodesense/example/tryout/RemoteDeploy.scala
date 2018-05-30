package ai.nodesense.example.tryout

import akka.actor.Actor.Receive
import akka.actor._
import akka.cluster.Cluster
import akka.remote.RemoteScope
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object RemoteDeploy extends App {

  val commonConfig = ConfigFactory.parseString(
    """
      |akka {
      |  actor {
      |    provider = "akka.cluster.ClusterActorRefProvider"
      |  }
      |  remote {
      |    netty.tcp {
      |      hostname = "127.0.0.1"
      |     }
      |  }
      |  cluster {
      |    seed-nodes = ["akka.tcp://cluster@127.0.0.1:2551"]
      |    metrics.enabled = off
      |  }
      |}
    """.stripMargin)

  implicit val system1 = ActorSystem("cluster", ConfigFactory.parseString(
    """
      |akka.remote.netty.tcp.port = 2551
    """.stripMargin).withFallback(commonConfig))

  implicit val system2 = ActorSystem("cluster", ConfigFactory.parseString(
    """
      |akka.remote.netty.tcp.port = 2552
    """.stripMargin).withFallback(commonConfig))

  class Dummy extends Actor with ActorLogging {
    log.info("Started")
    override def receive: Receive = Actor.emptyBehavior
  }

  Thread.sleep(3000)
  system1.actorOf(
    Props(classOf[Dummy]).withDeploy(Deploy(scope = RemoteScope(Address("akka.tcp", "cluster", "127.0.0.1", 2552)))),
    "dummy")


  StdIn.readLine()
  val bothDone = system1.terminate().zip(system2.terminate())
  Await.result(bothDone, Duration.Inf)

}