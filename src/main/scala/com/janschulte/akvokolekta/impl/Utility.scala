package com.janschulte.akvokolekta.impl

import com.yahoo.sketches.theta.{UpdateSketch, Sketch, Sketches}

/**
 * @author Jan Schulte <jan@janschulte.com>
 */
private[akvokolekta] object Utility {

  def createSketcher[T](size: Int, toHash: (T) => Long): (T) => Sketch = {
    val sketch = Sketches.updateSketchBuilder().build(size)
    (elem: T) => {
      sketch.update(toHash(elem))
      sketch
    }
  }

  def createUnionSketcher(): (Sketch) => Double = {
    val union = Sketches.setOperationBuilder().buildUnion()
    (sketch:Sketch) => {
      union.update(sketch)
      union.getResult.getEstimate
    }
  }
}
