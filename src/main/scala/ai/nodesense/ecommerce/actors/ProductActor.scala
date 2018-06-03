package ai.nodesense.ecommerce.actors

//#product-registry-actor
import akka.actor.{Actor, ActorLogging, Props}
//import example.actors.{Document, Email, NextID}
import scala.util.{Failure, Success}


// Krish
import ai.nodesense.ecommerce.db._

import scala.concurrent.ExecutionContext.Implicits.global;

//#product-case-classes
//
//final case class Product(name: String,
//                         price: Int,
//                         brandId: Int,
//                         )
//
//final case class Products(products: Seq[Product])

final case class ActionPerformed(description: String)
final case class ActionFailed(statusCode: Int, message: String)
//#product-case-classes

object ProductActor {
  final case object GetProducts
  final case class CreateProduct(product: Product)
  final case class UpdateProduct(product: Product)
  final case class GetProduct(id: Int)
  final case class DeleteProduct(id: Int)

  def props: Props = Props[ProductActor]
}

class ProductActor extends Actor with ActorLogging {
  import ProductActor._

  var products = Set.empty[Product]

  def receive: Receive = {
    case GetProducts => {
      //sender() ! Products(products.toSeq)
      val senderRef = sender();
      println("Querying products")
      ProductDao.findAll
        .onComplete {
          case Success(products) => {
            println("Got products ", products);
            // will not work
            //sender() ! Products(products.toSeq)
            senderRef ! Products(products.toSeq)
          }
        }
    }
    case CreateProduct(product) => {
      val senderRef = sender();
      println("creating  products")
      //      products += product
      //      sender() ! ActionPerformed(s"Product ${product.name} created.")
      ProductDao.create(product)
        .onComplete {
          case Success(productId) => {
            println("Inserted product ", productId);
            senderRef ! ActionPerformed(s"Product $productId created.")

            //context.actorSelection("../printerActor") ! Document( s"Product Created $productId", "New product arrived")

            //context.actorSelection("../emailActor") ! Email(NextID.getId, "ex@example.com", s"Product Created $productId", "New product arrived")

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


    case UpdateProduct(product) => {
      val senderRef = sender();
      println("creating  products")
      //      products += product
      //      sender() ! ActionPerformed(s"Product ${product.name} created.")
      ProductDao.update(product, product.id)
        .onComplete {
          case Success(productId) => {
            println("Updated products ", productId);
            senderRef ! ActionPerformed(s"Updated $productId products #${product.id} .")

          }

        }
    }

    case GetProduct(id) => {
      // sender() ! products.find(_.name == name)

      val senderRef = sender();
      println("Querying product ", id)
      ProductDao.findById(id)
        .onComplete {
          case Success(product) => {
            println("Got product ", product);
            senderRef ! Some(product)
          }

        }

    }

    case DeleteProduct(id) => {
      //products.find(_.name == id) foreach { product => products -= product }
      // sender() ! ActionPerformed(s"Product ${id} deleted.")

      val senderRef = sender();
      println("Deleting product ", id)
      ProductDao.delete(id)
        .onComplete {
          case Success(productId) => {
            println("Deleted product ", productId);
            senderRef !  ActionPerformed(s"Product ${id} gone - ${productId} product(s) deleted.")
          }

        }
    }

  }
}
//#product-registry-actor