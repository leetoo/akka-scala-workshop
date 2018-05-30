package ai.nodesense.example.tryout


import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.Await
// important - it needs to be this Seq and not the default one
import scala.collection.immutable.Seq

// this is just for the await, so not really needed
import scala.concurrent.duration._

object CsvUnmarshalling extends App {

  // the String => Seq[T] csv unmarshaller is here
  import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers.CsvSeq
  // but it needs something that will make each seq entry from String => T
  // in our case T is String, so wee need a String => String unmarshaller
  // (that doesn't really do anything - also known as `identity` in fp-speak)
  // it can be found here:
  import akka.http.scaladsl.unmarshalling.Unmarshaller.identityUnmarshaller

  implicit val system = ActorSystem()

  // we need a materializer and an execution context in the implicit scope as well
  implicit val mat = ActorMaterializer()
  import scala.concurrent.ExecutionContext.Implicits.global

  val csv = "1,iphone,20,50000,1000000,sales note\n"
  val seq = Unmarshal(csv).to[Seq[String]]
  println(Await.result(seq, 3.seconds))
}
