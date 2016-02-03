package com.janschulte.akvokolekta.graph

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import breeze.util.BloomFilter
import com.yahoo.sketches.theta.UpdateSketch

/**
  * @author Jan Schulte <jan@janschulte.com>
  */
case class DistinctCounter[A](k: Int = 4096, toHash: (A) => Long = (elem:A) => elem.hashCode().toLong) extends GraphStage[FlowShape[A, Double]] {

   val in = Inlet[A]("DistinctCounter.in")
   val out = Outlet[Double]("DistinctCounter.out")

   val shape = FlowShape.of(in, out)

   override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
     new GraphStageLogic(shape) {

       val sketch: UpdateSketch = UpdateSketch.builder.build(k)

       setHandler(in, new InHandler {
         override def onPush(): Unit = {
           val elem = grab(in)
           sketch.update(toHash(elem))
           push(out, sketch.getEstimate)
         }
       })
       setHandler(out, new OutHandler {
         override def onPull(): Unit = {
           pull(in)
         }
       })
     }
 }