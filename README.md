# chronos-parser-scala

cron 表記のパーサーを提供する Scala ライブラリです。

[![CI](https://github.com/j5ik2o/chronos-parser-scala/workflows/CI/badge.svg)](https://github.com/j5ik2o/chronos-parser-scala/actions?query=workflow%3ACI)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.j5ik2o/chronos-parser-scala_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.j5ik2o/chronos-parser-scala_2.13)
[![Renovate](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)](https://renovatebot.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## 動作環境

- JDK 17 以上（ビルド時は `--release 17` でコンパイル）
- 対応 Scala バージョン: 2.13.18 / 3.3.7（クロスビルド対応）

## インストール

sbt への追加例（2.13/3 共通）:

```scala
val version = "..."

libraryDependencies += "com.github.j5ik2o" %% "chronos-parser-scala" % version
```

## 使い方

```scala
val cronSchedule   = CronSchedule("*/1 * * * *", ZoneId.systemDefault())
val actuals        = cronSchedule.upcoming(Instant.now()).take(10)
assert(actuals(0) == start)
assert(actuals(1) == start.plus(Duration.ofMinutes(1)))
// ...
actuals.foreach(println)
```

## ライセンス

MIT License（[LICENSE](LICENSE) 参照）
