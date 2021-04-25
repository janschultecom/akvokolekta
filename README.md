# akvokolekta

[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

Akvokolekta is an extension of the akka streams library. It adds additional (memory-bounded) functionality for processing the stream that is currently not found in the akka streams library. 

# Features

* Deduplicate stream
* Count distinct elements
* Count union elements
* Count intersection elements

# Installation
Make sure your `build.sbt` contains the Sonatype snapshot resolver.
```scala
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
```
Then just add a library dependency.

```scala
libraryDependencies += "com.janschulte" %% "akvokolekta" % "0.1.0-SNAPSHOT"
```

# Usage

To use akvokolekta, add the following import:
```scala
import com.janschulte.akvokolekta.StreamAdditions._
```

### Deduplication

Deduplicate the stream using memory-bounded bloom filter.

##### Using scala dsl
```scala
val source = Source(List(1, 2, 3, 4, 1, 2, 3, 4))

// deduplicate a source
val deduplicated = source.deduplicate()
// prints 1 2 3 4 
deduplicated.runForeach(println)

// deduplicate a flow
val deduplicator = Flow[Int].deduplicate()
// prints 1 2 3 4
source.via(deduplicator).runForeach(println)
```

##### Using graph api
```scala
import com.janschulte.akvokolekta.graph.Deduplicator
import GraphDSL.Implicits._

val partial = GraphDSL.create() { implicit builder =>
  val source = builder.add(Broadcast[Int](1))
  val dedup = builder.add(Deduplicator[Int]())
  val sink = builder.add(Merge[Int](1))

  source ~> dedup ~> sink

  FlowShape(source.in, sink.out)
}
```

### Count distinct elements

Count the distinct elements of the stream using memory-bounded data sketches.

##### Using scala dsl
```scala
// count distinct elements from a source
val distincts = Source(List(1, 2, 1, 3, 2, 1, 3, 4)).countDistinct()

// prints 1.0 2.0 2.0 3.0 3.0 3.0 3.0 4.0
distincts.runForeach(println)

// count distinct elements from a flow
val source = Source(List(1, 2, 1, 3, 2, 1, 3, 4))
val distincts = Flow[Int].countDistinct()

// prints 1.0 2.0 2.0 3.0 3.0 3.0 3.0 4.0
source.via(distincts).runForeach(println)
```

##### Using graph api
```scala
import com.janschulte.akvokolekta.graph.Deduplicator
import GraphDSL.Implicits._

val partial = GraphDSL.create() { implicit builder =>
  val source = builder.add(Broadcast[Int](1))
  val counter = builder.add(DistinctCounter[Int]())
  val sink = builder.add(Merge[Double](1))

  source ~> counter ~> sink

  FlowShape(source.in, sink.out)
}
```

### Count union elements

Count the union of two streams using memory-bounded data sketches, i.e. the cardinality of the union of two streams |S1 ∪ S2|.

##### Using scala dsl
```scala
// estimate cardinality of union of two sources
val left = Source((0 to 10).toList)
val right = Source((5 to 14).toList)

// prints ... 15.0
left.countUnion(right).runForeach(println)

// estimate cardinality of union of source and flow
val union = Flow[Int].countUnion(right)

// prints ... 15.0
left.via(union).runForeach(println)
```

### Count intersecting elements

Count the intersection of two streams using memory-bounded data sketches, i.e. the cardinality of the intersection of two streams |S1 ∩ S2|.

##### Using scala dsl
```scala
// estimate cardinality of intersection of two sources
val left = Source((0 to 10).toList)
val right = Source((5 to 14).toList)

// prints ... 5.0
left.countIntersection(right).runForeach(println)

// estimate cardinality of intersection of source and flow
val intersection = Flow[Int].countIntersection(right)

// prints ... 5.0
left.via(intersection).runForeach(println)
```

# The giants
Like many other projects akvokolekta stands on the shoulders of giants:
* [Yahoo Data Sketches](http://datasketches.github.io/)
* [ScalaNLP](http://www.scalanlp.org/)


# Contributing

If you like to contribute, please create an issue and send a pull request. For more information on pull requests see the [Github pull request tutorial](https://help.github.com/articles/using-pull-requests).

