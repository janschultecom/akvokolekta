package com.janschulte.akvokolekta.test

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, FlowShape}
import com.janschulte.akvokolekta.StreamAdditions._
import com.janschulte.akvokolekta.graph.Deduplicator
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

/**
 * Specification for StreamAdditions
 */
class GraphAdditionsSpec extends Specification with NoTimeConversions {

  implicit val system = ActorSystem("test")
  implicit val mat = ActorMaterializer()

  "The GraphAdditions" should {

    val totalNumbers = 500000
    val distinctNumbers = 1000
    val sampleSize = 50000


    "deduplicate a flow" in {

      val elements = Seq.fill(totalNumbers)(Random.nextInt(distinctNumbers)).toList

      import GraphDSL.Implicits._

      val partial = GraphDSL.create() { implicit builder =>
        val source = builder.add(Broadcast[Int](1))
        val dedup = builder.add(Deduplicator[Int]())
        val sink = builder.add(Merge[Int](1))

        source ~> dedup ~> sink

        FlowShape(source.in, sink.out)
      }

      val deduplicator = Flow[Int].deduplicate()
      val eventualDeduplicated = Source(elements)
        .via(partial)
        .runFold(List.empty[Int])((acc, item) => item :: acc)

      val deduplicated = Await.result(eventualDeduplicated, 60 seconds)
      deduplicated must haveSize(distinctNumbers)
      deduplicated must containTheSameElementsAs(elements.distinct)
    }

  }

}
