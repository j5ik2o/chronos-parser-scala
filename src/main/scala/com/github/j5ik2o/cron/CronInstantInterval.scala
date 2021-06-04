package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.CronInstantInterval.NextInstantDuration
import com.github.j5ik2o.intervals.{ Interval, Limit, LimitValue, Limitless }

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
}

case class CronInstantInterval private (
    instantInterval: Interval[Instant],
    instantSpecification: CronInstantSpecification
) {

  val startValue: LimitValue[Instant] = instantInterval.lowerLimit
  val endValue: LimitValue[Instant]   = instantInterval.upperLimit

  def getInstantAfter(currentInstant: Instant, numberOfMinutes: Int): Option[Instant] = {
    require(numberOfMinutes >= 0)

    val itr                     = iterator.filterNot { instant => instant.isBefore(currentInstant) }
    var count                   = 0
    var result: Option[Instant] = None
    while (itr.hasNext && count < numberOfMinutes) {
      result = Some(itr.next())
      count += 1
    }
    result
  }

  private def createStream(
      _startValue: LimitValue[Instant],
      _endValue: LimitValue[Instant],
      nextStartValue: (LimitValue[Instant]) => LimitValue[Instant],
      predicate: (Instant, LimitValue[Instant]) => Boolean
  ): LazyList[Instant] = {
    LazyList
      .cons(
        _startValue.toValue,
        createStream(
          nextStartValue(_startValue),
          _endValue,
          nextStartValue,
          predicate
        )
      ).filter(instantSpecification)
      .takeWhile { v =>
        _endValue match {
          case _: Limitless[Instant] => true
          case Limit(end)            => predicate(v, end)
        }
      }
  }

  def toLazyList: LazyList[Instant] = {
    createStream(
      startValue,
      endValue,
      _.toValue.plus(NextInstantDuration),
      (instant, endInstant) => !instant.isAfter(endInstant)
    )
  }

  def toForwardLazyList: LazyList[Instant] = toLazyList

  def toReverseLazyList: LazyList[Instant] = {
    createStream(
      endValue,
      startValue,
      _.toValue.minus(NextInstantDuration),
      (instant, endInstant) => !instant.isBefore(endInstant)
    )
  }

  def iterator: Iterator[Instant] = toForwardLazyList.iterator

  def forwardIterator: Iterator[Instant] = iterator

  def reverseIterator: Iterator[Instant] = toReverseLazyList.iterator
}
