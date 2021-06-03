package com.github.j5ik2o.cron

import com.github.j5ik2o.intervals.{ Interval, Limit, LimitValue, Limitless }

import java.time._

class InstantInterval(startValue: LimitValue[Instant], endValue: LimitValue[Instant], interval: Duration)
    extends Interval[Instant](startValue, true, endValue, true)
    with Serializable {

  private def createStream(
      _startValue: LimitValue[Instant],
      _endValue: LimitValue[Instant],
      nextStartValue: (LimitValue[Instant]) => LimitValue[Instant],
      predicate: (Instant, LimitValue[Instant]) => Boolean
  ): LazyList[Instant] = {
    require(hasLowerLimit)
    LazyList
      .cons(
        _startValue.toValue,
        createStream(
          nextStartValue(_startValue),
          _endValue,
          nextStartValue,
          predicate
        )
      ).takeWhile { v =>
        _endValue match {
          case _: Limitless[Instant] => true
          case Limit(end)            => predicate(v, end)
        }
      }
  }

  def toLazyList: LazyList[Instant] = {
    if (!hasLowerLimit) {
      throw new IllegalStateException
    }
    createStream(
      lowerLimit,
      upperLimit,
      _startValue => _startValue.toValue.plus(interval),
      (instant, endInstant) => !instant.isAfter(endInstant)
    )
  }

  def toForwardLazyList: LazyList[Instant] = toLazyList

  def toReverseLazyList: LazyList[Instant] = {
    if (!hasUpperLimit) {
      throw new IllegalStateException
    }
    createStream(
      upperLimit,
      lowerLimit,
      _.toValue.minus(interval),
      (instant, endInstant) => !instant.isBefore(endInstant)
    )
  }

  def iterator: Iterator[Instant] = toForwardLazyList.iterator

  def forwardIterator: Iterator[Instant] = iterator

  def reverseIterator: Iterator[Instant] = toReverseLazyList.iterator

}

object InstantInterval {

  def apply(
      startValue: LimitValue[Instant],
      endValue: LimitValue[Instant]
  ): InstantInterval = apply(startValue, endValue, Duration.ofMinutes(1))

  def apply(
      startValue: LimitValue[Instant],
      endValue: LimitValue[Instant],
      interval: Duration
  ): InstantInterval =
    new InstantInterval(startValue, endValue, interval)

  def everFrom(startDate: LimitValue[Instant]): InstantInterval =
    everFrom(startDate, Duration.ofMinutes(1))

  def everFrom(startDate: LimitValue[Instant], interval: Duration): InstantInterval =
    inclusive(startDate, Limitless[Instant](), interval)

  def inclusive(
      startValue: LimitValue[Instant],
      endValue: LimitValue[Instant]
  ): InstantInterval =
    inclusive(startValue, endValue, Duration.ofMinutes(1))

  def inclusive(
      startValue: LimitValue[Instant],
      endValue: LimitValue[Instant],
      interval: Duration
  ): InstantInterval =
    new InstantInterval(startValue, endValue, interval)

  def inclusive(startYear: Year, startMonthDay: MonthDay, endYear: Year, endMonthDay: MonthDay): InstantInterval =
    inclusive(startYear, startMonthDay, endYear, endMonthDay, Duration.ofMinutes(1), ZoneId.systemDefault())

  def inclusive(
      startYear: Year,
      startMonthDay: MonthDay,
      endYear: Year,
      endMonthDay: MonthDay,
      zoneId: ZoneId
  ): InstantInterval = inclusive(startYear, startMonthDay, endYear, endMonthDay, Duration.ofMinutes(1), zoneId)

  def inclusive(
      startYear: Year,
      startMonthDay: MonthDay,
      endYear: Year,
      endMonthDay: MonthDay,
      interval: Duration
  ): InstantInterval = inclusive(startYear, startMonthDay, endYear, endMonthDay, interval, ZoneId.systemDefault())

  def inclusive(
      startYear: Year,
      startMonthDay: MonthDay,
      endYear: Year,
      endMonthDay: MonthDay,
      interval: Duration,
      zoneId: ZoneId
  ): InstantInterval = {
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
    new InstantInterval(Limit(startDate.toInstant), Limit(endDate.toInstant), interval)
  }
}
