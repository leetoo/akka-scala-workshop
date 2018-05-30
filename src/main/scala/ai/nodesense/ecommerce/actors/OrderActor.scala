package ai.nodesense.ecommerce.actors

//#order-registry-actor
import akka.actor.{Actor, ActorLogging, Props}
//import example.actors.{Document, Email, NextID}
import scala.util.{Failure, Success}

import ai.nodesense.ecommerce.db._

import scala.concurrent.ExecutionContext.Implicits.global;


//final case class ActionPerformed(description: String)
//final case class ActionFailed(statusCode: Int, message: String)
//#order-case-classes

object OrderActor {
  final case object GetOrders
  final case class CreateOrder(order: Order)
  final case class UpdateOrder(order: Order)
  final case class GetOrder(id: Int)
  final case class DeleteOrder(id: Int)

  def props: Props = Props[OrderActor]
}

class OrderActor extends Actor with ActorLogging {
  import OrderActor._

  var orders = Set.empty[Order]

  def receive: Receive = {
    case GetOrders => {
      //sender() ! Orders(orders.toSeq)
      println("Get orders from db")
      val senderRef = sender();
      println("Querying orders")
      OrderDao.findAll
        .onComplete {
          case Success(orders) => {
            println("Got orders ", orders);
            senderRef ! Orders(orders.toSeq)

          }

        }



    }
    case CreateOrder(order) => {
      val senderRef = sender();
      println("creating  orders")
      //      orders += order
      //      sender() ! ActionPerformed(s"Order ${order.name} created.")
      OrderDao.create(order)
        .onComplete {
          case Success(orderId) => {
            println("Inserted order ", orderId);
            senderRef ! ActionPerformed(s"Order $orderId created.")

            //context.actorSelection("../printerActor") ! Document( s"Order Created $orderId", "New order arrived")

            //context.actorSelection("../emailActor") ! Email(NextID.getId, "ex@example.com", s"Order Created $orderId", "New order arrived")

            //absolute path
            //context.actorSelection("/user/emailActor") !  Greeting("!!! using absolute  Email")

            // broadcast to all siblings, including self
            //context.actorSelection("../*") ! Greeting("!!! broadcasting")


          }

          case Failure(t: Throwable) => {
            println("Error happend in DB ", t.getMessage());
            senderRef ! ActionFailed(400, s"PRoduct Creation failed created ${t.getMessage()}")
          }

        }
    }


    case UpdateOrder(order) => {
      val senderRef = sender();
      println("creating  orders")
      //      orders += order
      //      sender() ! ActionPerformed(s"Order ${order.name} created.")
      OrderDao.update(order, order.id)
        .onComplete {
          case Success(orderId) => {
            println("Updated orders ", orderId);
            senderRef ! ActionPerformed(s"Updated $orderId orders #${order.id} .")

          }

        }
    }

    case GetOrder(id) => {
      // sender() ! orders.find(_.name == name)

      val senderRef = sender();
      println("Querying order ", id)
      OrderDao.findById(id)
        .onComplete {
          case Success(order) => {
            println("Got order ", order);
            senderRef ! Some(order)
          }

        }

    }

    case DeleteOrder(id) => {
      //orders.find(_.name == id) foreach { order => orders -= order }
      // sender() ! ActionPerformed(s"Order ${id} deleted.")

      val senderRef = sender();
      println("Deleting order ", id)
      OrderDao.delete(id)
        .onComplete {
          case Success(orderId) => {
            println("Deleted order ", orderId);
            senderRef !  ActionPerformed(s"Order ${id} gone - ${orderId} order(s) deleted.")
          }

        }
    }

  }
}
//#order-registry-actor