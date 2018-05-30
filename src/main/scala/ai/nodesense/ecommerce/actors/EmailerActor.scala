package ai.nodesense.ecommerce.actors


import akka.actor.{Actor, Props, Terminated}

class EmailerActor extends Actor  {
  import ai.nodesense.ecommerce.models.Email
  def receive = {

    case Email(to, subject, body) =>
      println(s"To deliver $to, $subject $body")
      //sender() ! EmailReportResponse(true)

    case _ => println("Unknown message")
  }
}