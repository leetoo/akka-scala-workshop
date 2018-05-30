package ai.nodesense.ecommerce.server

//#json-support
import ai.nodesense.ecommerce.actors.{ActionPerformed, User, Users}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import ai.nodesense.ecommerce.db._;

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)
  //
  //
  implicit val productJsonFormat = jsonFormat4(Product)
  implicit val productsJsonFormat = jsonFormat1(Products)


  implicit val orderJsonFormat = jsonFormat6(Order)
  implicit val ordersJsonFormat = jsonFormat1(Orders)


  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-support
