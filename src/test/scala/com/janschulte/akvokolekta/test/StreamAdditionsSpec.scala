package com.janschulte.akvokolekta.test

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import scala.concurrent.{Future, Await}
import scala.util.Random

import com.janschulte.akvokolekta.StreamAdditions._
import scala.concurrent.duration._

/**
 * Specification for StreamAdditions
 */
class StreamAdditionsSpec extends Specification with NoTimeConversions {

  implicit val system = ActorSystem("test")
  implicit val mat = ActorMaterializer()

  "The StreamAdditions" should {

    val totalNumbers = 500000
    val distinctNumbers = 1000
    val sampleSize = 50000

    "deduplicate a source" in {

      val elements = Seq.fill(totalNumbers)(Random.nextInt(distinctNumbers)).toList

      val eventualDeduplicated =
        Source(elements)
          .deduplicate()
          .runFold(List.empty[Int])((acc, item) => item :: acc)

      val deduplicated = Await.result(eventualDeduplicated, 60 seconds)
      deduplicated must haveSize(distinctNumbers)
      deduplicated must containTheSameElementsAs(elements.distinct)
    }

    "deduplicate a flow" in {

      val elements = Seq.fill(totalNumbers)(Random.nextInt(distinctNumbers)).toList

      val deduplicator = Flow[Int].deduplicate()
      val eventualDeduplicated = Source(elements)
        .via(deduplicator)
        .runFold(List.empty[Int])((acc, item) => item :: acc)

      val deduplicated = Await.result(eventualDeduplicated, 60 seconds)
      deduplicated must haveSize(distinctNumbers)
      deduplicated must containTheSameElementsAs(elements.distinct)
    }

    "count the distinct elements of a source" in {

      val elements: List[Long] = Random.shuffle(for {i <- 0 to totalNumbers} yield Random.nextInt(distinctNumbers)).map(_.toLong).toList

      val source = Source(elements)
      val eventualEstimatedDistinct = source
        .countDistinct()
        .take(sampleSize)
        .runFold(List.empty[Double])((acc, item) => item :: acc)
        .map(_.head)

      val estimatedDinstict: Double = Await.result(eventualEstimatedDistinct, 60 seconds)
      estimatedDinstict must be between(distinctNumbers * 0.95, distinctNumbers * 1.05)
    }

    "count the distinct elements of a flow" in {

      val elements: List[Long] = Random.shuffle(for {i <- 0 to totalNumbers} yield Random.nextInt(distinctNumbers)).map(_.toLong).toList

      val countFlow = Flow[Long].countDistinct()
      val eventualEstimatedDistinct = Source(elements)
        .via(countFlow)
        .take(sampleSize)
        .runFold(List.empty[Double])((acc, item) => item :: acc)
        .map(_.head)

      val estimatedDinstict: Double = Await.result(eventualEstimatedDistinct, 60 seconds)
      estimatedDinstict must be between(distinctNumbers * 0.95, distinctNumbers * 1.05)
    }

    "count the union of two flows" in {

      val samples: Int = 100000
      val leftValues: List[Int] = Seq.fill(samples)(Random.nextInt(4000)).toList
      val rightValues: List[Int] = Seq.fill(samples)(Random.nextInt(4000) + 3000).toList

      val union = Flow[Int].countUnion(Source(rightValues))

      val flow: Source[Double, NotUsed] = Source(leftValues)
        .via(union)

      val eventualUnionCount: Future[Double] = flow
        .runFold(0.0)(Math.max)

      val unionCount = Await.result(eventualUnionCount, 600 seconds)
      unionCount must beCloseTo(7000.0, 100.0)
    }
  }

}
