package ai.nodesense.example.routing


import akka.actor.Actor

class Worker extends Actor {
  import Worker._

  println("Worker actor created")

  def receive = {
    case msg: Work =>
      println(s"I received Work Message and My ActorRef: ${self} ${msg}")
  }
}

object Worker {
  case class Work(title: String) {
    override def toString(): String = s"Work $title"
  }
}