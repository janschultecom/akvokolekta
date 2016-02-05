package com.janschulte.akvokolekta.impl

import com.yahoo.sketches.theta.{UpdateSketch, Sketch, Sketches}

/**
 * @author Jan Schulte <jan@janschulte.com>
 */
private[akvokolekta] object Utility {

  def createSketcher[T](size: Int, toHash: (T) => Long): (T) => UpdateSketch = {
    val sketch = Sketches.updateSketchBuilder().build(size)
    (elem: T) => {
      sketch.update(toHash(elem))
      sketch
    }
  }

  def createUnionSketcher(): (UpdateSketch, UpdateSketch) => Double = {

    (left:UpdateSketch, right:UpdateSketch) => {
      val union = Sketches.setOperationBuilder().buildUnion()
      union.update(left)
      union.update(right)
      union.getResult.getEstimate
    }
  }

  def createIntersectionSketcher(k:Int): (UpdateSketch, UpdateSketch) => Double = {

    (left:UpdateSketch, right:UpdateSketch) => {
      val intersection = Sketches.setOperationBuilder().buildIntersection(k)
      intersection.update(left)
      intersection.update(right)
      val estimate: Double = intersection.getResult.getEstimate
      //println(s"Estimate: $estimate")
      estimate
    }
  }
}
