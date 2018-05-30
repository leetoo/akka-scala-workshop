package ai.nodesense.example.routing


import akka.actor.{ ActorSystem, Props }
import akka.routing.{ RandomGroup, FromConfig }
import Worker._

object Random extends App {

  val system = ActorSystem("Random-Router")

  val routerPool = system.actorOf(FromConfig.props(Props[Worker]), "random-router-pool")

  routerPool ! Work("Work 1")

  routerPool ! Work("Work 2")

  routerPool ! Work("Work 3")

  routerPool ! Work("Work 4")

  Thread.sleep(100)

  system.actorOf(Props[Worker], "w1")
  system.actorOf(Props[Worker], "w2")
  system.actorOf(Props[Worker], "w3")

  val paths = List("/user/w1", "/user/w2", "/user/w3")

  val routerGroup = system.actorOf(RandomGroup(paths).props(), "random-router-group")

  routerGroup ! Work("Work 5")

  routerGroup ! Work("Work 6")

  routerGroup ! Work("Work 7")

  routerGroup ! Work("Work 8")

  Thread.sleep(100)

  system.terminate()
}