package ai.nodesense.ecommerce.db

/*

create table orders(id INT AUTO_INCREMENT,
  productId INT NOT NULL,
  customerId INT NOT NULL,
  customerAccount INT NOT NULL,
   amount INT NOT NULL,
  merchantAccount INT NOT NULL,
primary key (id));

*/
// id - primary key
case class Order(id: Option[Int],
                 productId: Int,
                 customerId: Int,
                 customerAccount: Int,
                 amount: Int,
                 merchantAccount: Int);

final case class Orders(orders: Seq[Order])