package ai.nodesense.ecommerce.actors

import ai.nodesense.ecommerce.models.Document
import akka.actor.{Actor, ActorLogging, ActorRef, RootActorPath, Terminated}
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp, UnreachableMember}
import akka.cluster._
import akka.routing.RoundRobinGroup

class PrinterServiceActor extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  var routees = Set[String]()

  def buildRouter() = context.actorOf(RoundRobinGroup(routees).props)
  var router = buildRouter

  def addRoutee(ref: akka.actor.ActorRef): Unit = {
    println("** Add routee", ref.path.toString)

    routees += ref.path.toString
    router = buildRouter
  }

  def removeRoutee(ref: akka.actor.ActorRef): Unit = {
    routees -= ref.path.toString
    router = buildRouter
  }


  println("printer service ", this);

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {

    case PrinterRegistration if !routees.contains(sender().path.toString) =>
      println(" Adding a actor ")
      context watch sender()
      addRoutee(sender())


    case Terminated(a) =>
      removeRoutee(a)

    case MemberUp(member) =>
      println("Printer service actor ", member)
      val consumerRootPath = RootActorPath(member.address)
      println("Root Path ", consumerRootPath);

      if(member.hasRole("printer")){
        //backends = backends :+ member
        println("New Printer member added");
      }

    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)

    case document:Document =>
      println(s"printer service got a document to print, now forward")
      router ! document
  }
}

