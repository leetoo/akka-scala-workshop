package ai.nodesense.ecommerce.actors

import akka.actor.{Actor, Props, Terminated}
import akka.routing.{ ActorRefRoutee, RoundRobinRoutingLogic, Router }

class PrinterMasterActor extends Actor {
  import ai.nodesense.ecommerce.models.Document

  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[PrinterActor])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case document: Document ⇒
      println("Printer Master Received, shall route  ", document)
      router.route(document, sender())
    case Terminated(a) ⇒
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[PrinterActor])
      context watch r
      router = router.addRoutee(r)
  }
}