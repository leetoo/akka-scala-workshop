package ai.nodesense.ecommerce.db

import slick.driver.MySQLDriver.api._


class OrdersTable(tag: Tag) extends Table[Order](tag, "orders") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)


  def productId = column[Int]("productId")

  def customerId = column[Int]("customerId")


  def customerAccount = column[Int]("customerAccount")

  def amount = column[Int]("amount")

  def merchantAccount = column[Int]("merchantAccount")


  def * = (id.?, productId, customerId, customerAccount, amount, merchantAccount) <> ((Order.apply _).tupled, Order.unapply)
}

