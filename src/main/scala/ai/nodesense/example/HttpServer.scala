package ai.nodesense.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.actor.{Actor, ActorLogging, Props}

import akka.event.Logging

import scala.concurrent.Await


import scala.concurrent.duration._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import akka.http.scaladsl.server.directives._
import ContentTypeResolver.Default


import akka.http.scaladsl.model.{ HttpEntity,
                                  ContentTypes,
                                  HttpRequest,
                                  HttpResponse,
                                  Uri
}

import akka.http.scaladsl.model.HttpMethods.{
                                              GET, PUT, POST, DELETE, OPTIONS
                                            }

import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import akka.stream.ActorMaterializer

case class Product(id: Int,
                   name: String );

final case class Products(products: Seq[Product])

final case object GetProducts;
final case class GetProduct(id: Int)

final case class CreateProduct(product: Product)
final case class UpdateProduct(product: Product)
final case class DeleteProduct(id: Int)

final case class ProductActionPerformed(description: String)
final case class ActionFailed(statusCode: Int, message: String)


trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val productJsonFormat = jsonFormat2(Product)
  implicit val productsJsonFormat = jsonFormat1(Products)

  implicit  val productActionPerformed = jsonFormat1(ProductActionPerformed)

}

//#user-routes-class
trait Routes extends JsonSupport {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Routes])

  // other dependencies that ProductRoutes use
  def productActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  lazy val productRoutes: Route =
    pathPrefix("products") {
      concat(
        //#products-get-delete
        pathEnd {
          concat(
            get {
              val products: Future[Products] =
                (productActor ? GetProducts).mapTo[Products]
              complete(products)
            },
            post {
              entity(as[Product]) { product =>
                val productCreated: Future[ProductActionPerformed] =
                  (productActor ? CreateProduct(product)).mapTo[ProductActionPerformed]
                // FIXME: OnFailure
                onSuccess(productCreated) { performed =>
                  log.info("Created product [{}]: {}", product.name, performed.description)
                  // FIXME
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        //#products-get-post
        //#products-get-delete

        //GET /products/1
        // PUT /products/1
        // DELETE /products/1

        path(Segment) { id =>
          concat(
            get {
              //#retrieve-product-info
              val maybeProduct: Future[Option[Product]] =
                (productActor ? GetProduct(id.toInt)).mapTo[Option[Product]]
              rejectEmptyResponse {
                complete(maybeProduct)
              }
              //#retrieve-product-info
            },

            put {
              entity(as[Product]) { product =>
                val productUpdated: Future[ProductActionPerformed] =
                  (productActor ? UpdateProduct(product)).mapTo[ProductActionPerformed]
                onSuccess(productUpdated) { performed =>
                  log.info("Update product [{}]: {}", product.name, performed.description)
                  // FIXME
                  complete((StatusCodes.Created, performed))

                }
              }
            },

            delete {
              //#products-delete-logic
              val productDeleted: Future[ProductActionPerformed] =
                (productActor ? DeleteProduct(id.toInt)).mapTo[ProductActionPerformed]
              onSuccess(productDeleted) { performed =>
                log.info("Deleted product [{}]: {}", id, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#products-delete-logic
            }
          )
        }
      )
      //#products-get-delete
    }
  //#all-routes
}

object HttpServer  extends App  with Routes {

  class ProductActor extends Actor with ActorLogging {
    var products = Set.empty[Product]

    def receive: Receive = {
      case GetProducts => {
        //sender() ! Products(products.toSeq)
        val senderRef = sender();
        println("Querying products")
        senderRef ! Products(products.toSeq)
      }
      case CreateProduct(product) => {
        val senderRef = sender();
        println("creating  products")
        products += product
        sender() ! ProductActionPerformed(s"Product ${product.name} created.")

      }


      case UpdateProduct(product) => {
        val senderRef = sender();
        println("creating  products")
        products += product
        sender() ! ProductActionPerformed(s"Product ${product.name} created.")
      }

      case GetProduct(id) => {
        sender() ! products.find(_.id == id)
      }

      case DeleteProduct(id) => {
        products.find(_.id == id) foreach { product => products -= product }
        sender() ! ProductActionPerformed(s"Product ${id} deleted.")
      }
    }
  }

  object ProductActor {

    def props: Props = Props[ProductActor]
  }

  implicit val system: ActorSystem = ActorSystem("actors")
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  val productActor: ActorRef = system.actorOf(ProductActor.props, "productActor")


  val profileRoute = path("profile") {
    put {
      // Works with PUT http://localhost:8080/profile?id=100&user=Admin

      parameter("id".as[Int], "user") { (bid, user) =>
        // place a bid, fire-and-forget
        //auction ! Bid(user, bid)
        println("updated ");
        complete((StatusCodes.Accepted, "profile updated "))
      }
    } ~
      get {
        implicit val timeout: Timeout = 5.seconds

        // query the actor for the current auction state
        //val bids: Future[Bids] = (auction ? GetBids).mapTo[Bids]
        //complete(bids)
        complete(StatusCodes.Accepted, "profile take it ")
      }
  }

  // low level model

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        "<html><body>Hello world!</body></html>"))

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sys.error("BOOM!")

    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }


  val fileRoutes = path("logs" / Segment) { name =>
      println("Log path is", name)
      getFromFile(s"$name.log") // uses implicit ContentTypeResolver
    }

  var dirRoutes = pathPrefix("data") {

      getFromDirectory("/Users/krish/workshops/akka-scala-workshop/data")
    }

  Http().bindAndHandle(route ~ productRoutes ~ profileRoute ~ fileRoutes ~ dirRoutes, "localhost", 8080)


  // Low Level API Example
  //Http().bindAndHandleSync(requestHandler, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}
