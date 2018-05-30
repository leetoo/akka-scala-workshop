package ai.nodesense.ecommerce.actors

import akka.actor.{Actor, ActorLogging}


class PrinterActor extends Actor with ActorLogging {
  import ai.nodesense.ecommerce.models.Document

  log.debug("PrinterActor Created")

  def receive = {
    case document: Document =>
      println("Document Received by Printer ", document)
      log.info(s"PrinterActor received Document $document")
    //println(sender())

    case _ => println("Unknown document")
  }
}