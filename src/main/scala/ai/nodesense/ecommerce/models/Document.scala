package ai.nodesense.ecommerce.models

case class Document(title: String,
                    body: String) {

  override def toString: String = s"Document $title, $body"

}
