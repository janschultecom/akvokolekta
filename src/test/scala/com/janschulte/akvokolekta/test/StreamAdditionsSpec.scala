package com.janschulte.akvokolekta.test

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import scala.concurrent.Await
import scala.util.Random

import com.janschulte.akvokolekta.StreamAdditions._
import scala.concurrent.duration._
/**
 * Specification for OsmWayPointMapper
 */
class StreamAdditionsSpec extends Specification with NoTimeConversions{

  implicit val system = ActorSystem("test")
  implicit val mat = ActorMaterializer()


  "The FlowAdditions" should {

    val totalNumbers = 5000000
    val distinctNumbers = 10000
    val sampleSize = 50000

    "deduplicate a source" in {

      val elements = Seq.fill(totalNumbers)(Random.nextInt(distinctNumbers)).toList

      val eventualDeduplicated =
        Source(elements)
        .deduplicate(distinctNumbers, 0.001)
        .runFold(List.empty[Int])((acc, item) => item :: acc)

      val deduplicated = Await.result(eventualDeduplicated, 10 seconds)
      deduplicated must haveSize(distinctNumbers)
      deduplicated must containTheSameElementsAs(elements.distinct)
    }

    "deduplicate a flow" in {

      val elements = Seq.fill(totalNumbers)(Random.nextInt(distinctNumbers)).toList

      val deduplicator = Flow[Int].deduplicate(distinctNumbers,0.001)
      val eventualDeduplicated = Source(elements)
        .via(deduplicator)
        .runFold(List.empty[Int])((acc, item) => item :: acc)

      val deduplicated = Await.result(eventualDeduplicated, 10 seconds)
      deduplicated must haveSize(distinctNumbers)
      deduplicated must containTheSameElementsAs(elements.distinct)
    }

    "count the distinct elements of a source" in {

      val elements:List[Long] = Random.shuffle(for { i <- 0 to totalNumbers } yield Random.nextInt(distinctNumbers)).map(_.toLong).toList

      val source = Source(elements)
      val eventualEstimatedDistinct = source
        .countDistinct(k = 4096, toHash = (x:Long) => x)
        .take(sampleSize)
        .runFold(List.empty[Double])((acc, item) => item :: acc)
        .map(_.head)

      val estimatedDinstict: Double = Await.result(eventualEstimatedDistinct, 1 second)
      estimatedDinstict must be between(distinctNumbers * 0.95, distinctNumbers * 1.05)
    }

    "count the distinct elements of a flow" in {

      val elements:List[Long] = Random.shuffle(for { i <- 0 to totalNumbers } yield Random.nextInt(distinctNumbers)).map(_.toLong).toList

      val countFlow = Flow[Long].countDistinct(k = 4096, toHash = identity)
      val eventualEstimatedDistinct = Source(elements)
        .via(countFlow)
        .take(sampleSize)
        .runFold(List.empty[Double])((acc, item) => item :: acc)
        .map(_.head)

      val estimatedDinstict: Double = Await.result(eventualEstimatedDistinct, 1 second)
      estimatedDinstict must be between(distinctNumbers * 0.95, distinctNumbers * 1.05)
    }

  }

}
