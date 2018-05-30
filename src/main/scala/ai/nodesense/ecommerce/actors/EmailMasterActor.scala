package ai.nodesense.ecommerce.actors


import akka.actor.{Actor, Props, Terminated}
import akka.routing.{ ActorRefRoutee, RoundRobinRoutingLogic, Router }

class EmailMasterActor extends Actor {
  import ai.nodesense.ecommerce.models.Email

  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[EmailerActor])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case email: Email ⇒
      router.route(email, sender())
    case Terminated(a) ⇒
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[EmailerActor])
      context watch r
      router = router.addRoutee(r)
  }
}