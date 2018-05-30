package ai.nodesense.ecommerce.server

import ai.nodesense.ecommerce.actors.{ActionPerformed, User, Users}
import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import ai.nodesense.ecommerce.actors.UserRegistryActor._
import ai.nodesense.ecommerce.actors.ProductActor._

import ai.nodesense.ecommerce.actors.OrderActor._
import akka.pattern.ask
import akka.util.Timeout
import ai.nodesense.ecommerce.db._

//#user-routes-class
trait Routes extends JsonSupport {
  //#user-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Routes])

  // other dependencies that UserRoutes use
  def userRegistryActor: ActorRef

  // other dependencies that ProductRoutes use
  def productActor: ActorRef

  def orderActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //#all-routes
  //#users-get-post
  //#users-get-delete
  lazy val userRoutes: Route =
  pathPrefix("users") {
    concat(
      //#users-get-delete
      //GET  localhost:8080/users  LIST of all users
      //POST localhost:8080/users  CREATE NEW user

      pathEnd {
        concat(
          get {
            val users: Future[Users] =
              (userRegistryActor ? GetUsers).mapTo[Users]
            complete(users)
          },
          post {
            entity(as[User]) { user =>
              val userCreated: Future[ActionPerformed] =
                (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
              onSuccess(userCreated) { performed =>
                log.info("Created user [{}]: {}", user.name, performed.description)
                complete((StatusCodes.Created, performed))
              }
            }
          }
        )
      },
      //#users-get-post
      //#users-get-delete
      // GET http://localhost:8080/users/user1  get single user
      // DELETE http://localhost:8080/users/user1 delete sigle user
      path(Segment) { name =>
        concat(
          get {
            //#retrieve-user-info
            val maybeUser: Future[Option[User]] =
              (userRegistryActor ? GetUser(name)).mapTo[Option[User]]
            rejectEmptyResponse {
              complete(maybeUser)
            }
            //#retrieve-user-info
          },
          delete {
            //#users-delete-logic
            val userDeleted: Future[ActionPerformed] =
              (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
            onSuccess(userDeleted) { performed =>
              log.info("Deleted user [{}]: {}", name, performed.description)
              complete((StatusCodes.OK, performed))
            }
            //#users-delete-logic
          }
        )
      }
    )
    //#users-get-delete
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
                val productCreated: Future[ActionPerformed] =
                  (productActor ? CreateProduct(product)).mapTo[ActionPerformed]
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
                val productUpdated: Future[ActionPerformed] =
                  (productActor ? UpdateProduct(product)).mapTo[ActionPerformed]
                onSuccess(productUpdated) { performed =>
                  log.info("Update product [{}]: {}", product.name, performed.description)
                  // FIXME
                  complete((StatusCodes.Created, performed))

                }
              }
            },

            delete {
              //#products-delete-logic
              val productDeleted: Future[ActionPerformed] =
                (productActor ? DeleteProduct(id.toInt)).mapTo[ActionPerformed]
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


  lazy val orderRoutes: Route =
    pathPrefix("orders") {
      concat(
        //#orders-get-delete
        pathEnd {
          concat(
            get {
              println("Get orders api")
              val orders: Future[Orders] =
                (orderActor ? GetOrders).mapTo[Orders]
              complete(orders)
            },
            post {
              entity(as[Order]) { order =>
                val orderCreated: Future[ActionPerformed] =
                  (orderActor ? CreateOrder(order)).mapTo[ActionPerformed]
                // FIXME: OnFailure
                onSuccess(orderCreated) { performed =>
                  log.info("Created order [{}]: {}", order.id, performed.description)
                  // FIXME
                  complete((StatusCodes.Created, performed))
                }




              }
            }
          )
        },
        //#orders-get-post
        //#orders-get-delete

        //GET /orders/1
        // PUT /orders/1
        // DELETE /orders/1

        path(Segment) { id =>
          concat(
            get {
              //#retrieve-order-info
              val maybeOrder: Future[Option[Order]] =
                (orderActor ? GetOrder(id.toInt)).mapTo[Option[Order]]
              rejectEmptyResponse {
                complete(maybeOrder)
              }
              //#retrieve-order-info
            },

            put {
              entity(as[Order]) { order =>
                val orderUpdated: Future[ActionPerformed] =
                  (orderActor ? UpdateOrder(order)).mapTo[ActionPerformed]
                onSuccess(orderUpdated) { performed =>
                  log.info("Update order [{}]: {}", order.id, performed.description)
                  // FIXME
                  complete((StatusCodes.Created, performed))

                }
              }
            },

            delete {
              //#orders-delete-logic
              val orderDeleted: Future[ActionPerformed] =
                (orderActor ? DeleteOrder(id.toInt)).mapTo[ActionPerformed]
              onSuccess(orderDeleted) { performed =>
                log.info("Deleted order [{}]: {}", id, performed.description)
                complete((StatusCodes.OK, performed))
              }
              //#orders-delete-logic
            }
          )
        }
      )
      //#orders-get-delete
    }

  //#all-routes
}
