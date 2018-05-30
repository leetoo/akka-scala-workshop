package ai.nodesense.ecommerce.db


import slick.driver.MySQLDriver.api._
import scala.concurrent.Future

object ProductDao extends Dao {
  def findAll: Future[Seq[Product]] = productsTable.result

  def findById(productId: Int): Future[Product] = productsTable.filter(_.id === productId).result.head
  def create(product: Product): Future[Int] = productsTable returning productsTable.map(_.id) += product
  def update(newProduct: Product, productId: Option[Int]): Future[Int] = productsTable.filter(_.id === productId)
    .map(product => (product.name, product.price, product.brandId))
    .update((newProduct.name, newProduct.price, newProduct.brandId))

  def delete(productId: Int): Future[Int] = productsTable.filter(_.id === productId).delete
}

