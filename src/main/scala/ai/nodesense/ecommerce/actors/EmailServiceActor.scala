package ai.nodesense.ecommerce.actors

import ai.nodesense.ecommerce.models.Email
import akka.actor.{Actor, ActorLogging, ActorRef, Props, RootActorPath, Terminated}
import akka.cluster.ClusterEvent.{MemberRemoved, MemberUp, UnreachableMember}
import akka.cluster._
import akka.routing.{ConsistentHashingGroup, FromConfig, RoundRobinGroup, RoundRobinPool}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}


class EmailServiceActor extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  val router = context.actorOf(RoundRobinPool(3).props(Props[EmailerActor]))

  /*
  var backends = IndexedSeq.empty[ActorRef]
  var routees = Set[String]()
  def buildRouter() = context.actorOf(RoundRobinGroup(routees).props)
  var router = buildRouter
  */


//  def addRoutee(ref: akka.actor.ActorRef): Unit = {
//    println("** Add routee", ref.path.toString)
//
//    routees += ref.path.toString
//    router = buildRouter
//  }
//
//  def removeRoutee(ref: akka.actor.ActorRef): Unit = {
//    routees -= ref.path.toString
//    router = buildRouter
//  }


  println("email service ", this);


  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberUp])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive = {

//    case EmailerRegistration if !backends.contains(sender()) =>
//      println(" Adding a actor ")
//      context watch sender()
//      backends = backends :+ sender()
//      addRoutee(sender())
//
//
//    case Terminated(a) =>
//      backends = backends.filterNot(_ == a)
//      removeRoutee(a)

    case MemberUp(member) =>
       println("Email service actor ", member)
      val consumerRootPath = RootActorPath(member.address)
      println("Root Path ", consumerRootPath);


      if(member.hasRole("emailer")){
        //backends = backends :+ member
        println("New Emailer member added");
      }

    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)


    case e:Email =>
      println(s"email service got email, now forward")
      println("****" + context.system.settings.config.getString("host-port"))

      router ! e

  }

}

