# akvokolekta

[![Join the chat at https://gitter.im/janschultecom/akvokolekta](https://badges.gitter.im/janschultecom/akvokolekta.svg)](https://gitter.im/janschultecom/akvokolekta?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/janschultecom/akvokolekta.svg?branch=master)](https://travis-ci.org/janschultecom/akvokolekta)
[![Dependency Status](https://www.versioneye.com/user/projects/56acc94b7e03c7003ba41334/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56acc94b7e03c7003ba41334)

Akvokolekta is an extension of the akka streams library. It adds additional (memory-bounded) functionality for processing the stream that is currently not found in the akka streams library. 

# Features

* Stream deduplication
* Count distinct elements of a stream.

# Installation
akvokolekta will be published shortly on sonatype. In the meantime, you have to build & publish manually:
```sh
git clone https://github.com/janschultecom/akvokolekta.git
cd akvokolekta
sbt publishLocal
```
Add the following line to your build.sbt

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
// deduplicates a source
Source(List(1,2,3,4,1,2,3,4)
  .deduplicate()


// deduplicates a flow
Flow[Int]
  .deduplicate()
```

### Count distinct elements

Count the distinct elements of the stream using memory-bounded data sketches.
```scala
// deduplicates a source
Source(List(1,2,3,4,1,2,3,4)
  .countDistinct()
// Returns a stream that emits the current count of distinct elements.

// deduplicates a flow
Flow[Int]
  .countDistinct()
```

# The giants

* [Yahoo Data Sketches](http://datasketches.github.io/)
* [ScalaNLP](http://www.scalanlp.org/)


# Contributing

If you like to contribute, please create an issue and send a pull request. For more information on pull requests see the [Github pull request tutorial](https://help.github.com/articles/using-pull-requests).

