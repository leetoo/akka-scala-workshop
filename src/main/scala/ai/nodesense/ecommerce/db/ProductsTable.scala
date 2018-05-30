package ai.nodesense.ecommerce.db

import slick.driver.MySQLDriver.api._

class ProductsTable(tag: Tag) extends Table[Product](tag, "products") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def price = column[Int]("price")
  def brandId = column[Int]("brandId")
  def * = (id.?, name, price, brandId) <> ((Product.apply _).tupled, Product.unapply)
}

