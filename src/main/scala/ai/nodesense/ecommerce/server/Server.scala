package ai.nodesense.ecommerce.server

import akka.actor.Status.Success
import ai.nodesense.ecommerce.db._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import slick.jdbc.MySQLProfile.api._
import scala.util.{Success, Failure}

import scala.concurrent.duration._
import scala.concurrent.Future


import scala.concurrent.ExecutionContext.Implicits.global

object  Server   {

    def slickGetStarted = {
      val driver = slick.driver.MySQLDriver

      import driver.api._

      def db = Database.forConfig("mysql")

      //  def db = Database.forURL(
      //    "jdbc:mysql://localhost:3306/productsdb?user=root&password=",
      //    driver = "com.mysql.jdbc.Driver"
      //  )

      implicit val session: Session = db.createSession()

      var products = TableQuery[ProductsTable];

      println(products.schema.createStatements.mkString)


      def productsData = Seq(
        Product(None, "Product 200", 200, 1)
      )

      val insert: DBIO[Option[Int]] = products ++= productsData

      var insertFuture = db.run(insert);

      val productId = Await.result(insertFuture, 2.seconds)
      println(s"Inserted $productId");


     // val productsAction: DBIO[Seq[Product]] = products.result
//
//     val productsFuture: Future[Seq[Product]] = db.run(productsAction)
//
//
//      val productsResults = Await.result(productsFuture, 2.seconds)
//
//      productsResults.foreach(println(_));
    }


    slickGetStarted
}
