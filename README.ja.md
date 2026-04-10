# chronos-parser-scala

[English](README.md)

cron 式のパースと評価を行う Scala ライブラリです。

[![CI](https://github.com/j5ik2o/chronos-parser-scala/workflows/CI/badge.svg)](https://github.com/j5ik2o/chronos-parser-scala/actions?query=workflow%3ACI)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.j5ik2o/chronos-parser-scala_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.j5ik2o/chronos-parser-scala_2.13)
[![Renovate](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)](https://renovatebot.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## 動作環境

- JDK 17 以上（`--release 17` でコンパイル）
- Scala 2.13.18 / 3.3.7（クロスビルド対応）

## インストール

`build.sbt` に以下を追加してください:

```scala
libraryDependencies += "com.github.j5ik2o" %% "chronos-parser-scala" % "<version>"
```

## 使い方

```scala
import com.github.j5ik2o.cron.CronSchedule
import java.time.{Instant, ZoneId}

val cronSchedule = CronSchedule("*/3 * * * *", ZoneId.systemDefault())
val upcoming = cronSchedule.upcoming(Instant.now()).take(5)
upcoming.foreach(println)
```

### API 概要

- `CronSchedule(expr, zoneId)` - cron 式からスケジュールを作成
- `schedule.upcoming(start)` - cron 式にマッチする時刻の遅延リストを取得
- `schedule.getInstantAfter(base, minutes)` - 指定した分数以内で次にマッチする時刻を取得
- `CronParser.parse(expr)` - cron 式をパースして AST に変換

### 対応する cron 構文

| フィールド   | 値              | 特殊文字            |
|--------------|-----------------|---------------------|
| 分           | 0-59            | `*` `,` `-` `/`    |
| 時           | 0-23            | `*` `,` `-` `/`    |
| 日           | 1-31            | `*` `,` `-` `/` `L`|
| 月           | 1-12            | `*` `,` `-` `/`    |
| 曜日         | 0-7 (SUN-SAT)   | `*` `,` `-` `/`    |

## ライセンス

MIT License（[LICENSE](LICENSE) 参照）
