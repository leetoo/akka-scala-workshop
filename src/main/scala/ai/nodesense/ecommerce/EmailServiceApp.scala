package ai.nodesense.ecommerce

import ai.nodesense.ecommerce.actors.{EmailServiceActor, EmailerActor}
import ai.nodesense.ecommerce.models.Email
import ai.nodesense.ecommerce.services.ClusterListener
import akka.actor.{ActorPath, ActorSystem, Props}
import akka.cluster.client.{ClusterClient, ClusterClientReceptionist, ClusterClientSettings}
import com.typesafe.config.ConfigFactory

object EmailServiceApp   {

  val config = ConfigFactory.load("email-service");

  println("Config ", config);

  //val system = ActorSystem("EmailersService", config)

  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      startup(Seq("2525"))
    else
      startup(args)
  }

  def startup(ports: Seq[String]): Unit = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
        withFallback(ConfigFactory.load("email-service"))

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)

      // Create an actor that handles cluster domain events
      //system.actorOf(Props[ClusterListener], name = "clusterListener")
      val emailService = system.actorOf(Props[EmailServiceActor], name = "emailService")
      system.actorOf(Props[EmailerActor], name = "emailerActor1")
      system.actorOf(Props[EmailerActor], name = "emailerActor2")
//
//      ClusterClientReceptionist(system).registerService(emailService)
//
//      val initialContacts = Set(
//        ActorPath.fromString("akka.tcp://ClusterSystem@localhost:2552/system/receptionist"),
//        ActorPath.fromString("akka.tcp://ClusterSystem@localhost:2551/system/receptionist"),
//        ActorPath.fromString("akka.tcp://ClusterSystem@localhost:2525/system/receptionist")
//      )
//
//      val settings = ClusterClientSettings(system)
//        .withInitialContacts(initialContacts)
//
//      val c = system.actorOf(ClusterClient.props(
//        ClusterClientSettings(system).withInitialContacts(initialContacts)), "client")


      Thread.sleep(5000);
      println("Sending msg");

      emailService ! Email("urgent@example.com", "Test", "welcome")
      emailService ! Email("urgent2@example.com", "Test", "welcome")

//
//
//      c ! ClusterClient.Send("/user/emailService", Email("TT@TT.com", "HISH", "TEST"), localAffinity = true)
//      c ! ClusterClient.SendToAll("/user/emailService", Email("TT@TT.com", "HISH", "TEST"))

    }
  }

}
