# chronos-parser-scala

A cron expression parser.

[![CI](https://github.com/j5ik2o/chronos-parser-scala/workflows/CI/badge.svg)](https://github.com/j5ik2o/chronos-parser-scala/actions?query=workflow%3ACI)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.j5ik2o/chronos-parser-scala_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.j5ik2o/chronos-parser-scala_2.13)
[![Scaladoc](http://javadoc-badge.appspot.com/com.github.j5ik2o/chronos-parser-scala_2.13.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.github.j5ik2o/chronos-parser-scala_2.13/com/github/j5ik2o/cron/index.html?javadocio=true)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Installation

Add the following to your sbt build (2.13.x):

```scala
val version = "..."

libraryDependencies += Seq(
  "com.github.j5ik2o" %% "chronos-parser-scala" % version,
)
```

## Usage

```scala
val cronSchedule   = CronSchedule("*/1 * * * *", ZoneId.systemDefault())
val actuals        = cronSchedule.upcoming(Instant.now()).take(10)
assert(actuals(0) == start)
assert(actuals(1) == start.plus(Duration.ofMinutes(1)))
actuals.foreach(println)

// 2021-06-03T22:33:16.093Z
// 2021-06-03T22:34:16.093Z
// 2021-06-03T22:35:16.093Z
// 2021-06-03T22:36:16.093Z
// 2021-06-03T22:37:16.093Z
// 2021-06-03T22:38:16.093Z
// 2021-06-03T22:39:16.093Z
// 2021-06-03T22:40:16.093Z
// 2021-06-03T22:41:16.093Z
// 2021-06-03T22:42:16.093Z
```

## License

MIT license ([LICENSE-MIT](LICENSE-MIT) or https://opensource.org/licenses/MIT)
