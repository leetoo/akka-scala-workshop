package ai.nodesense.ecommerce

import ai.nodesense.ecommerce.actors.PrinterMasterActor
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object  PrintersApp extends  App {
  import ai.nodesense.ecommerce.models.Document

  def deploy() = {

    val config = ConfigFactory.load.getConfig("PrintersService")

    val system = ActorSystem("PrintersService", config)

    val printersMaster = system.actorOf(Props[PrinterMasterActor], "printers")

    val printers = system.actorSelection("akka.tcp://PrintersService@127.0.0.1:2560/user/printers")

    printers ! Document("Order #1234 Confirmation", "Congrats, your order confirmed")
  }

  def remoteAddPrinter() = {

    val remoteConfig = ConfigFactory.parseString(
      """
        |akka {
        |  actor {
        |    provider = "akka.remote.RemoteActorRefProvider"
        |
        |    deployment {
        |       /printers2 {
        |         remote: "akka.tcp://PrintersService@127.0.0.1:2560"
        |       }
        |    }
        |  }
        |}
      """.stripMargin)

      println("Config ", remoteConfig)


    val system = ActorSystem("PrinterConsumer", remoteConfig)

    val printers2Actor = system.actorOf(Props[PrinterMasterActor], "printers2")

    println(s"The remote path of worker Actor is ${printers2Actor.path}")

    //prints akka.tcp://PrintersService@127.0.0.1:2560/remote/akka.tcp/PrinterConsumer@192.168.0.100:2552/user/printers2

    printers2Actor ! Document("Hi Remote Worker", "Print my document ")

    // TODO: select remote actor by selection

  }

  deploy()

  // some time for actor to boot
  println("Waiting for printers actor to boot")
  Thread.sleep(5000)

  println("Adding an actor to remote system ");
  remoteAddPrinter()

}
