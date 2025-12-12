package com.github.j5ik2o.cron

import com.github.j5ik2o.intervals.Limit
import org.scalatest.funsuite.AnyFunSuite

import java.time.{Duration, Instant, ZoneId, ZonedDateTime}

class CronInstantIntervalSpec extends AnyFunSuite {

  def createPlusInstantStream(value: Instant, duration: Duration, end: Instant): LazyList[Instant] =
    LazyList
      .cons(value, createPlusInstantStream(value.plus(duration), duration, end))
      .takeWhile(_.compareTo(end) <= 0)

  def createMinusInstantStream(
    value: Instant,
    duration: Duration,
    end: Instant): LazyList[Instant] =
    LazyList
      .cons(value, createMinusInstantStream(value.minus(duration), duration, end))
      .takeWhile(_.compareTo(end) >= 0)

  val zoneId: ZoneId = ZoneId.systemDefault()

  test("iterator") {
    val cronExpression = "*/2 * * * *"
    val expr = CronParser.parse(cronExpression)
    val start = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, zoneId).toInstant
    val end = start.plus(Duration.ofMinutes(20))
    val cii =
      CronInstantInterval.inclusive(
        Limit(start),
        Limit(end),
        CronInstantSpecification.of(expr, zoneId)
      )
    val list = cii.iterator.toList
    val expected = createPlusInstantStream(start, Duration.ofMinutes(2), end).toList
    assert(list == expected)
    list.take(10).foreach(println)
  }
  test("reverseIterator") {
    val cronExpression = "*/2 * * * *"
    val expr = CronParser.parse(cronExpression)
    val start = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, zoneId).toInstant
    val end = start.plus(Duration.ofMinutes(20))
    val cii = CronInstantInterval.inclusive(
      Limit(start),
      Limit(end),
      CronInstantSpecification.of(expr, zoneId)
    )
    val list = cii.reverseIterator.toList
    val expected = createMinusInstantStream(end, Duration.ofMinutes(2), start).toList
    assert(list == expected)
    list.take(10).foreach(println)
  }
  test("getInstantAfter") {
    val cronExpression = "*/2 * * * *"
    val expr = CronParser.parse(cronExpression)
    val start = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, zoneId).toInstant
    val end = start.plus(Duration.ofMinutes(20))
    val cii = CronInstantInterval.inclusive(
      Limit(start),
      Limit(end),
      CronInstantSpecification.of(expr, zoneId)
    )
    val result = for { min <- 0 to 20 by 2 } yield {
      val now = ZonedDateTime.of(2016, 1, 1, 0, min, 0, 0, zoneId).toInstant
      val instant = cii.getInstantAfter(now, 1).get
      (now, instant)
    }
    val (expected, actual) = result.unzip
    assert(actual == expected)
    actual.take(10).foreach(println)
  }
}
