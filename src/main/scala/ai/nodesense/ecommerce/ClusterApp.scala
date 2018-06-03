package ai.nodesense.ecommerce
import java.net._

import scala.concurrent.ExecutionContext.Implicits.global
import ai.nodesense.ecommerce.actors.{EmailServiceActor, EmailerActor}
import ai.nodesense.ecommerce.models.Email
import ai.nodesense.ecommerce.services.ClusterListener
import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

object ClusterApp   {

  def displayIpPort(port: String) = {

    val localhost: InetAddress = InetAddress.getLocalHost
    val localIpAddress: String = localhost.getHostAddress

    println(s"Cluster starting on => $localIpAddress:$port")
  }


  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
     startup(Seq("2551", "2552"))
     // startup(Seq("0"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      displayIpPort(port)
      // Override the configuration of the port
      val config = ConfigFactory.parseString(s"""
          akka.remote.netty.tcp.port=$port
           host-port=$port
      """
      ).

        withFallback(ConfigFactory.load("cluster-application"))



      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)

      println("****" + system.settings.config.getString("host-port"))

      // Create an actor that handles cluster domain events
      system.actorOf(Props[ClusterListener], name = "clusterListener")


       Cluster(system) registerOnMemberUp {
        system.actorOf(Props[EmailServiceActor], name = "emailService")
      }


        val f = Future {
          Thread.sleep(10000)
          println("Sedning messages");
          system.actorSelection("/user/emailService") ! Email("admin@example.com", s"1 Cluster Up $port", "Now all run well")
          system.actorSelection("/user/emailService") ! Email("admin@example.com", s"2 Cluster Up $port", "Now all run well")
          system.actorSelection("/user/emailService") ! Email("admin@example.com", s"3 Cluster Up $port", "Now all run well")
          system.actorSelection("/user/emailService") ! Email("admin@example.com", s"4 Cluster Up $port", "Now all run well")

        }


        f.onComplete {
          case Success(value) => println(s"Got the callback, meaning = $value")
          case Failure(e) => println("Error "); e.printStackTrace
        }



      Thread.sleep(2000)

    }
  }

}
