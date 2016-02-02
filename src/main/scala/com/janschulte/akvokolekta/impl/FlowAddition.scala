package com.janschulte.akvokolekta.impl

import akka.stream.{FlowShape, SourceShape}
import akka.stream.scaladsl.{Source, Flow}
import breeze.util.BloomFilter
import com.sun.corba.se.impl.orbutil.graph.Graph
import com.yahoo.sketches.theta.{Sketch, UpdateReturnState, Sketches, UpdateSketch}

/**
 * @author Jan Schulte <jan@janschulte.com>
 */
case class FlowAddition[I, O, M](flow: Flow[I, O, M]) {

  /**
   * Deduplicates the flow using a memory-bounded probabilistic bloom filter
   * @param expectedNumItems the number of distinct items expected that pass through the flow
   * @param falsePositiveRate the desired rate of false positives
   * @return a flow with removed duplicates
   */
  def deduplicate(expectedNumItems: Double = 100000, falsePositiveRate: Double = 0.0001): Flow[I, O, M] = {
    val bloom: BloomFilter[O] = BloomFilter.optimallySized[O](expectedNumItems,falsePositiveRate)

    flow
      .filter((elem) => {
        if (!bloom.contains(elem)) {
          bloom += elem
          true
        } else {
          false
        }
      }
      )
  }


  /**
   * Counts the distinct elements of a flow using a probabilistic sketch. The count is memory bounded with size k * 8 bytes.
   * @param k The size of the hash, the higher the more accurate. See [[http://datasketches.github.io/docs/KMVupdateVkth.html]].
   * @param toHash Hash function for the elements
   * @return A Flow emitting count estimates
   */
  def countDistinct(k: Int = 4096, toHash: (O) => Long = (elem) => elem.hashCode().toLong): Flow[I, Double, M] = {
    val sketch: UpdateSketch = UpdateSketch.builder.build(k)

    flow
      .map((elem) => sketch.update(toHash(elem)))
      .map(_ => sketch.getEstimate)
  }

  /**
   * Counts the number of elements of the union of this flow and the other source using a probabilistic sketch. The union is memory bounded.
   * @param other The source to union.
   * @param k The size of the hash, the higher the more accurate. See [[http://datasketches.github.io/docs/KMVupdateVkth.html]].
   * @param toHash Hash function for the elements
   * @return An estimate of |A ∪ B|
   */
  def countUnion(
                  other: Source[O, M],
                  k: Int = 4096, 
                  toHash: (O) => Long = (elem) => elem.hashCode().toLong): Flow[I, Double, M] = {

    val updateSketch = Utility.createSketcher(k, toHash)

    val left = flow.map(updateSketch)
    val right = other.map(updateSketch)

    val unionSketcher = Utility.createUnionSketcher()
    left
      .merge(right)
      .map(unionSketcher)
  }
}
