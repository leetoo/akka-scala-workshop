package ai.nodesense.example.routing


import akka.actor.{Props, ActorSystem}
import Worker._

object RouterApp extends App {

  val system = ActorSystem("router")

  val router = system.actorOf(Props[RouterPool])

  router ! Work("Work 1")

  router ! Work("Work 2")

  router ! Work("Work 3")

  Thread.sleep(100)

  system.actorOf(Props[Worker], "w1")
  system.actorOf(Props[Worker], "w2")
  system.actorOf(Props[Worker], "w3")
  system.actorOf(Props[Worker], "w4")
  system.actorOf(Props[Worker], "w5")

  val workers: List[String] = List(
    "/user/w1",
    "/user/w2",
    "/user/w3",
    "/user/w4",
    "/user/w5")

  val routerGroup = system.actorOf(Props(classOf[RouterGroup], workers))

  routerGroup ! Work("Work 4")

  routerGroup ! Work("Work 5")

  Thread.sleep(100)

  system.terminate()
}