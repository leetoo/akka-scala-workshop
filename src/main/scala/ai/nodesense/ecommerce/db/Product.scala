package ai.nodesense.ecommerce.db

// id - primary key
case class Product(id: Option[Int],
                   name: String,
                   price: Int,

                   brandId: Int);

final case class Products(products: Seq[Product])

