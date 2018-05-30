package ai.nodesense.ecommerce.db

trait Config {
  val driver = slick.driver.MySQLDriver

  import driver.api._

  def db = Database.forConfig("mysql")

  //  def db = Database.forURL(
  //    "jdbc:mysql://localhost:3306/productsdb?user=root&password=",
  //    driver = "com.mysql.jdbc.Driver"
  //  )

  implicit val session: Session = db.createSession()
}
