package ai.nodesense.ecommerce.db

import slick.dbio.{ Effect, NoStream }
import slick.lifted.TableQuery
import slick.sql.{ FixedSqlStreamingAction, SqlAction }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Dao extends Config {

  val productsTable = TableQuery[ProductsTable]
  val ordersTable = TableQuery[OrdersTable]


  protected implicit def executeFromDb[A](action: SqlAction[A, NoStream, _ <: slick.dbio.Effect]): Future[A] = {
    val future = db.run(action)
    future.onComplete { case _ => session.close() }

    future
  }

  protected implicit def executeReadStreamFromDb[A](action: FixedSqlStreamingAction[Seq[A], A, _ <: slick.dbio.Effect]): Future[Seq[A]] = {
    val future = db.run(action)
    future.onComplete { case _ => session.close() }

    future
  }


}
