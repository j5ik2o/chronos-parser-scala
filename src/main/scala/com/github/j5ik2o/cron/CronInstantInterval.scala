package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.CronInstantInterval._
import com.github.j5ik2o.intervals.{Interval, IntervalLimit, Limit, LimitValue, Limitless}

import java.time._

object CronInstantInterval {

  def apply(
    startValue: LimitValue[Instant],
    endValue: LimitValue[Instant],
    instantSpecification: CronInstantSpecification
  ): CronInstantInterval =
    new CronInstantInterval(Interval.closed(startValue, endValue), instantSpecification)

  def everFrom(
    startValue: LimitValue[Instant],
    instantSpecification: CronInstantSpecification
  ): CronInstantInterval =
    inclusive(startValue, Limitless[Instant](), instantSpecification)

  def inclusive(
    startValue: LimitValue[Instant],
    endValue: LimitValue[Instant],
    instantSpecification: CronInstantSpecification
  ): CronInstantInterval =
    apply(startValue, endValue, instantSpecification)

  def inclusive(
    startYear: Year,
    startMonthDay: MonthDay,
    endYear: Year,
    endMonthDay: MonthDay,
    instantSpecification: CronInstantSpecification,
    zoneId: ZoneId
  ): CronInstantInterval = {
    val startDate =
      ZonedDateTime.of(
        LocalDate.of(startYear.getValue, startMonthDay.getMonthValue, startMonthDay.getDayOfMonth),
        LocalTime.MIDNIGHT,
        zoneId
      )
    val endDate =
      ZonedDateTime.of(
        LocalDate.of(endYear.getValue, endMonthDay.getMonthValue, endMonthDay.getDayOfMonth),
        LocalTime.MIDNIGHT,
        zoneId
      )
    apply(Limit(startDate.toInstant), Limit(endDate.toInstant), instantSpecification)
  }

  final val NextInstantDuration: Duration = Duration.ofMinutes(1)

  private def createLazyList(
    startValue: LimitValue[Instant],
    endValue: LimitValue[Instant],
    getNextStartValue: (LimitValue[Instant]) => LimitValue[Instant],
    predicate: (Instant, LimitValue[Instant]) => Boolean,
    instantSpecification: CronInstantSpecification
  ): LazyList[Instant] =
    LazyList
      .cons(
        startValue.toValue,
        createLazyList(
          getNextStartValue(startValue),
          endValue,
          getNextStartValue,
          predicate,
          instantSpecification
        )
      )
      .filter(instantSpecification)
      .takeWhile { v =>
        endValue match {
          case _: Limitless[Instant] => true
          case Limit(end) => predicate(v, end)
        }
      }

}

case class CronInstantInterval private (
  underlying: Interval[Instant],
  instantSpecification: CronInstantSpecification
) {
  val lowerIntervalLimit: IntervalLimit[Instant] = underlying.lowerLimitObject
  val upperIntervalLimit: IntervalLimit[Instant] = underlying.upperLimitObject

  val lowerLimit: LimitValue[Instant] = underlying.lowerLimit
  val upperLimit: LimitValue[Instant] = underlying.upperLimit

  val startValue: LimitValue[Instant] = lowerLimit
  val endValue: LimitValue[Instant] = upperLimit

  def complementRelativeTo(other: Interval[Instant]): Seq[Interval[Instant]] =
    underlying.complementRelativeTo(other)

  def covers(other: Interval[Instant]): Boolean = underlying.covers(other)

  val emptyOfSameType: Interval[Instant] = underlying.emptyOfSameType

  def gap(other: Interval[Instant]): Interval[Instant] = underlying.gap(other)

  val hasLowerLimit: Boolean = underlying.hasLowerLimit

  val hasUpperLimit: Boolean = underlying.hasUpperLimit

  def includes(value: LimitValue[Instant]): Boolean = underlying.includes(value)

  val includesLowerLimit: Boolean = underlying.includesLowerLimit

  val includesUpperLimit: Boolean = underlying.includesUpperLimit

  def intersect(other: Interval[Instant]): Interval[Instant] = underlying.intersect(other)

  def intersects(other: Interval[Instant]): Boolean = underlying.intersects(other)

  def isAbove(value: LimitValue[Instant]): Boolean = underlying.isAbove(value)

  def isBelow(value: LimitValue[Instant]): Boolean = underlying.isBelow(value)

  def isClosed: Boolean = underlying.isClosed

  def isEmpty: Boolean = underlying.isEmpty

  def isOpen: Boolean = underlying.isOpen

  def isSingleElement: Boolean = underlying.isSingleElement

  def newOfSameType(
    lower: LimitValue[Instant],
    lowerClosed: Boolean,
    upper: LimitValue[Instant],
    upperClosed: Boolean
  ): Interval[Instant] = underlying.newOfSameType(lower, lowerClosed, upper, upperClosed)

  def getInstantAfter(currentInstant: Instant, numberOfMinutes: Int): Option[Instant] = {
    require(numberOfMinutes >= 0)

    val itr = iterator.filterNot(_.isBefore(currentInstant))
    var count = 0
    var result: Option[Instant] = None
    while (itr.hasNext && count < numberOfMinutes) {
      result = Some(itr.next())
      count += 1
    }
    result
  }

  def toLazyList: LazyList[Instant] =
    createLazyList(
      startValue,
      endValue,
      _.toValue.plus(NextInstantDuration),
      (instant, endInstant) => !instant.isAfter(endInstant),
      instantSpecification
    )

  def toForwardLazyList: LazyList[Instant] = toLazyList

  def toReverseLazyList: LazyList[Instant] =
    createLazyList(
      endValue,
      startValue,
      _.toValue.minus(NextInstantDuration),
      (instant, endInstant) => !instant.isBefore(endInstant),
      instantSpecification
    )

  def iterator: Iterator[Instant] = toForwardLazyList.iterator

  def forwardIterator: Iterator[Instant] = iterator

  def reverseIterator: Iterator[Instant] = toReverseLazyList.iterator
}
