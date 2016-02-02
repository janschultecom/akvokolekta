package com.janschulte.akvokolekta.impl

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import breeze.util.BloomFilter
import com.yahoo.sketches.theta.UpdateSketch

/**
 * @author Jan Schulte <jan@plasmap.io>
 */
case class SourceAddition[O,M](source:Source[O,M]) {

  /**
   * Deduplicates the source using a probabilistic bloom filter
   * @param expectedNumItems the number of distinct items expected from the source
   * @param falsePositiveRate the desired rate of false positives
   * @return a source with removed duplicates
   */
  def deduplicate(expectedNumItems: Double = 1000, falsePositiveRate: Double = 0.001): Source[O, M] = {
    val bloom: BloomFilter[O] = BloomFilter.optimallySized[O](expectedNumItems,falsePositiveRate)

    source
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
   * Counts the distinct elements emitted from the source using a probabilistic sketch. The count is memory bounded with size k * 8 bytes.
   * @param k The size of the hash, the higher the more accurate. See [[http://datasketches.github.io/docs/KMVupdateVkth.html]].
   * @param toHash Hash function for the elements
   * @return A source emitting count estimates
   */
  def countDistinct(k: Int = 4096, toHash: (O) => Long = (elem) => elem.hashCode().toLong): Source[Double, M] = {
    val sketch: UpdateSketch = UpdateSketch.builder.build(k)

    source
      .map((elem) => sketch.update(toHash(elem)))
      .map(_ => sketch.getEstimate)
  }

  /**
   * Counts the number of elements of the union of this source and the other source using a probabilistic sketch. The union is memory bounded.
   * @param other The source to union.
   * @param k The size of the hash, the higher the more accurate. See [[http://datasketches.github.io/docs/KMVupdateVkth.html]].
   * @param toHash Hash function for the elements
   * @return An estimate of |A ∪ B|
   */
  def countUnion(
                  other: Source[O, M],
                  k: Int = 4096,
                  toHash: (O) => Long = (elem) => elem.hashCode().toLong): Source[Double, M] = {

    val updateSketch = Utility.createSketcher(k, toHash)

    val left = source.map(updateSketch)
    val right = other.map(updateSketch)

    val unionSketcher = Utility.createUnionSketcher()
    left
      .merge(right)
      .map(unionSketcher)
  }
}
