package ai.nodesense.example.cluster

import akka.actor._
import akka.cluster.{Cluster, MemberStatus}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object GracefulLeaveClusterApp extends App {

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

  val system1 = ActorSystem(
    "cluster",
    ConfigFactory.parseString("akka.remote.netty.tcp.port = 2551").withFallback(commonConfig))

  val system2 = ActorSystem(
    "cluster",
    ConfigFactory.parseString("akka.remote.netty.tcp.port = 2552").withFallback(commonConfig))

  class Dummy extends Actor with ActorLogging {
    log.info("Started")

    override def receive: Receive = Actor.emptyBehavior
  }

  while (Cluster(system2).state.members.toSeq.size != 2 && Cluster(system2).state.members.forall(_.status == MemberStatus.up)) {
    Thread.sleep(250)
  }
  println("Both cluster nodes started (but possibly not UP both)")

  val node1 = Cluster(system1)
  val node2 = Cluster(system2)

  node2.registerOnMemberRemoved {
    println("Node 2 removed, terminating")
    system2.terminate()
  }
  println("Enter to make terminate node 2")
  StdIn.readLine()
  println("Node 2 terminating")
  system2.terminate()

  node1.registerOnMemberRemoved {
    println("Node 1 removed, terminating")
    system1.terminate()
  }

  println("Enter to make node 1 leave")
  StdIn.readLine()
  println("Node 1 leaving")
  node1.leave(node1.selfAddress)

  println("Enter to mark node 2 as down manually")
  StdIn.readLine()
  node1.down(node2.selfAddress)

  val bothDone = system1.whenTerminated.zip(system2.whenTerminated)
  Await.result(bothDone, Duration.Inf)

}