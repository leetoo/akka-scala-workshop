package ai.nodesense.ecommerce

import ai.nodesense.ecommerce.actors.PrinterMasterActor
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object  EmailersApp extends  App {
  import ai.nodesense.ecommerce.models.Document


  def deployFrontend(port: Integer) = {

    val config = ConfigFactory.load.getConfig("EmailersServiceFrontend")

    println("Config ", config);

    val system = ActorSystem("EmailersService", config)

    //val printersMaster = system.actorOf(Props[PrinterMasterActor], "emailers")

    //val printers = system.actorSelection("akka.tcp://PrintersService@127.0.0.1:2560/user/printers")

    // printers ! Document("Order #1234 Confirmation", "Congrats, your order confirmed")
  }


  def deployBackend(port: Integer) = {

    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.load().getConfig("EmailersServiceBackend"))

    println("Config ", config);

    val system = ActorSystem("EmailersService", config)

    //val printersMaster = system.actorOf(Props[PrinterMasterActor], "emailers")

   // val printers = system.actorSelection("akka.tcp://PrintersService@127.0.0.1:2560/user/printers")

    //printers ! Document("Order #1234 Confirmation", "Congrats, your order confirmed")
  }


  println("Starting Seed Front End at 2551")
  // Seed config/frontend
  deployFrontend(2551)
  Thread.sleep(3000)


  println("Starting backend End at 2552")
  deployBackend(2552)


  println("Starting backend End at 2553")
  deployBackend(2553)

  // some time for actor to boot
  println("Waiting for emailers actor to boot")
  Thread.sleep(5000)

}
