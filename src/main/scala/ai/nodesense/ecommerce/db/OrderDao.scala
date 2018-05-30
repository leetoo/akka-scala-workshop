package ai.nodesense.ecommerce.db


import slick.driver.MySQLDriver.api._
import scala.concurrent.Future

object OrderDao extends Dao {
  def findAll: Future[Seq[Order]] = ordersTable.result

  def findById(orderId: Int): Future[Order] = ordersTable.filter(_.id === orderId).result.head
  def create(order: Order): Future[Int] = ordersTable returning ordersTable.map(_.id) += order
  def update(newOrder: Order, orderId: Option[Int]): Future[Int] = ordersTable.filter(_.id === orderId)
    .map(order => (order.productId, order.customerId, order.customerAccount, order.amount, order.merchantAccount))
    .update((newOrder.productId, newOrder.customerId, newOrder.customerAccount, newOrder.amount, newOrder.merchantAccount))

  def delete(orderId: Int): Future[Int] = ordersTable.filter(_.id === orderId).delete
}

