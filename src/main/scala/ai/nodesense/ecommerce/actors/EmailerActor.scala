package ai.nodesense.ecommerce.actors


import akka.actor.{Actor, Props, RootActorPath, Terminated}
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.MemberUp

case object EmailerRegistration;

class EmailerActor extends Actor  {
  import ai.nodesense.ecommerce.models.Email

  println("Emailer Actor ", context)
  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)


  def receive = {

    case Email(to, subject, body) =>
      println(s"To deliver $to, $subject $body")
      //sender() ! EmailReportResponse(true)

    case MemberUp(m) => println("Member up now"); register(m)

    case _ => println("Unknown message")
  }


  def register(member: Member): Unit = {
    println("Email actor register member ", member)
    if (member.hasRole("emailService"))
      context.actorSelection(RootActorPath(member.address) / "user" / "emailService") !
        EmailerRegistration
    }


}