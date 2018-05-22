package ai.nodesense.example.paths



import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Identify, ActorIdentity }

class Watcher extends Actor {
  import Counter._

  var counterRef: ActorRef = _

  val selection = context.actorSelection("/user/counter")

  selection ! Identify(None)
  selection ! Inc(2)
  selection ! Dec(-1)

  def receive = {
    case ActorIdentity(_, Some(ref)) =>
      println(s"Actor Reference for counter is ${ref}")
    case ActorIdentity(_, None) =>
      println("Actor selection for actor doesn't live :( ")

  }
}