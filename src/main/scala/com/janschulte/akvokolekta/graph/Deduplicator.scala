package com.janschulte.akvokolekta.graph

import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import breeze.util.BloomFilter

/**
 * @author Jan Schulte <jan@janschulte.com>
 */
case class Deduplicator[A](expectedNumItems: Double = 100000, falsePositiveRate: Double = 0.0001) extends GraphStage[FlowShape[A, A]] {

  val in = Inlet[A]("Deduplicator.in")
  val out = Outlet[A]("Deduplicator.out")

  val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      val bloom: BloomFilter[A] = BloomFilter.optimallySized[A](expectedNumItems,falsePositiveRate)

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val elem = grab(in)
          if (!bloom.contains(elem)) {
            bloom += elem
            push(out, elem)
          }
          else pull(in)
        }
      })
      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
}