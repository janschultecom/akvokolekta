package com.janschulte.akvokolekta

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.janschulte.akvokolekta.impl.{FlowAddition, SourceAddition}


/**
 * Advanced utility functions for processing streams.
 *
 * @author Jan Schulte <jan@janschulte.com>
 */
object StreamAdditions {

  implicit def toFlowAddition[I, O, M](flow: Flow[I, O, M]): FlowAddition[I, O, M] = FlowAddition(flow)

  implicit def toStreamAddition[O, M](source: Source[O,M]): SourceAddition[O,M] = SourceAddition(source)

}


