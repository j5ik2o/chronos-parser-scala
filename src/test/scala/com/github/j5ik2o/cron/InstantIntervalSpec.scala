package com.github.j5ik2o.cron

import com.github.j5ik2o.intervals.Limit
import org.scalatest.funsuite.AnyFunSuite

import java.time.{ Duration, Instant, ZoneId, ZonedDateTime }

class InstantIntervalSpec extends AnyFunSuite {

  def createPlusInstantStream(value: Instant, duration: Duration, end: Instant): LazyList[Instant] =
    LazyList.cons(value, createPlusInstantStream(value.plus(duration), duration, end)).takeWhile(_.compareTo(end) <= 0)

  def createMinusInstantStream(value: Instant, duration: Duration, end: Instant): LazyList[Instant] =
    LazyList
      .cons(value, createMinusInstantStream(value.minus(duration), duration, end)).takeWhile(_.compareTo(end) >= 0)

  test("iterator") {
    val start: Instant = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant
    val end: Instant   = start.plus(Duration.ofMinutes(1))
    val duration       = Duration.ofSeconds(2)
    val interval       = InstantInterval.inclusive(Limit(start), Limit(end), Duration.ofSeconds(2))
    val list           = interval.iterator.toList
    val expected       = createPlusInstantStream(start, duration, end).toList
    assert(list == expected)
    list.take(10).foreach(println)
  }

  test("reverseIterator") {
    val start: Instant = ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant
    val end: Instant   = start.plus(Duration.ofMinutes(1))
    val duration       = Duration.ofSeconds(2)
    val interval       = InstantInterval.inclusive(Limit(start), Limit(end), Duration.ofSeconds(2))
    val list           = interval.reverseIterator.toList
    val expected       = createMinusInstantStream(end, duration, start).toList
    assert(list == expected)
    list.take(10).foreach(println)
  }

}
