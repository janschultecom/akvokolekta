# akvokolekta

[![Join the chat at https://gitter.im/janschultecom/akvokolekta](https://badges.gitter.im/janschultecom/akvokolekta.svg)](https://gitter.im/janschultecom/akvokolekta?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/janschultecom/akvokolekta.svg?branch=master)](https://travis-ci.org/janschultecom/akvokolekta)
[![Coverage Status](https://coveralls.io/repos/github/janschultecom/akvokolekta/badge.svg?branch=master)](https://coveralls.io/github/janschultecom/akvokolekta?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/56acc94b7e03c7003ba41334/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56acc94b7e03c7003ba41334)
[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

Akvokolekta is an extension of the akka streams library. It adds additional (memory-bounded) functionality for processing the stream that is currently not found in the akka streams library. 

# Features

* Deduplicate stream
* Count distinct elements

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

### Count distinct elements

Count the distinct elements of the stream using memory-bounded data sketches.
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

# The giants

* [Yahoo Data Sketches](http://datasketches.github.io/)
* [ScalaNLP](http://www.scalanlp.org/)


# Contributing

If you like to contribute, please create an issue and send a pull request. For more information on pull requests see the [Github pull request tutorial](https://help.github.com/articles/using-pull-requests).

