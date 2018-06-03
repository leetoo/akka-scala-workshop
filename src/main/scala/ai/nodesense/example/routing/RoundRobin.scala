package ai.nodesense.example.routing


import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.routing.{ RoundRobinPool, FromConfig }
import Worker._

object RoundRobin extends App {

  val system = ActorSystem("Round-Robin-Router")

  val routerPool = system.actorOf(RoundRobinPool(3).props(Props[Worker]),
                            "round-robin-pool")

  routerPool ! Work("Work 1")

  routerPool ! Work("Work 2")

  routerPool ! Work("Work 3")

  routerPool ! Work("Work 4")


  routerPool ! Work("Work 4")

  routerPool ! Work("Work 5")

  Thread.sleep(100)

  //  system.actorOf(Props[Worker], "w1")
  //system.actorOf(Props[Worker], "w2")
  // system.actorOf(Props[Worker], "w3")


//  val routerGroup = system.actorOf(FromConfig.props(), "round-robin-group")
//
//  routerGroup ! Work("Work 5")
//
//  routerGroup ! Work("Work 6")
//
//  routerGroup ! Work("Work 7")
//
//  routerGroup ! Work("Work 8")

  Thread.sleep(100)

  // system.shutdown()
}