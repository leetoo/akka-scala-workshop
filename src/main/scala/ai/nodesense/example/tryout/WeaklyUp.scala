package ai.nodesense.example.tryout

import akka.actor.{Actor, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{MemberEvent, ReachabilityEvent}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object WeaklyUp extends App {

  def config(port: Int) =
    ConfigFactory.parseString(
      s"""
         |akka {
         |  loglevel = ERROR
         |  actor {
         |    provider = "akka.cluster.ClusterActorRefProvider"
         |  }
         |  remote {
         |    log-remote-lifecycle-events = off
         |    netty.tcp {
         |      hostname = "localhost"
         |      port = $port
         |     }
         |  }
         |  cluster {
         |    allow-weakly-up-members = on,
         |    seed-nodes = ["akka.tcp://cluster@localhost:2551"]
         |    metrics.enabled = off
         |  }
         |}
      """.
        stripMargin)

  class Snitch extends Actor {
    def receive = { case x => println(x) }
  }
  val system1 = ActorSystem("cluster", config(2551))

  val snitch = system1.actorOf(Props(new Snitch))
  Cluster(system1).subscribe(snitch, classOf[MemberEvent], classOf[ReachabilityEvent])

  val system2 = ActorSystem("cluster", config(2552))
  val system3 = ActorSystem("cluster", config(2553))

  println("ENTER to kill node 2 and join a new node")
  StdIn.readLine()

  println("terminating system2 it will make it unreachable")
  Await.ready(system2.terminate(), 20.seconds)

  // and then joining a new node makes the new node weakly up
  val newNodeJoining = ActorSystem("cluster", config(2554))

  StdIn.readLine()
  System.exit(0)

}