package com.github.j5ik2o.cron

import com.github.j5ik2o.intervals.{ Interval, Limit, LimitValue, Limitless }

import java.time.{ Duration, Instant, LocalDate, LocalTime, MonthDay, Year, ZoneId, ZonedDateTime }

class CronInstantInterval(
    startValue: LimitValue[Instant],
    endValue: LimitValue[Instant],
    instantSpecification: CronInstantSpecification
) extends Interval[Instant](startValue, true, endValue, true)
    with Serializable {

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

  def toInstantInterval: InstantInterval = InstantInterval.inclusive(startValue, endValue, Duration.ofMinutes(1))

  def toLazyList: LazyList[Instant] = toInstantInterval.toForwardLazyList.filter(instantSpecification)

  def toForwardLazyList: LazyList[Instant] = toLazyList

  def iterator: Iterator[Instant] = toForwardLazyList.iterator

  def forwardIterator: Iterator[Instant] = iterator

  def toReverseLazyList: LazyList[Instant] = toInstantInterval.toReverseLazyList.filter(instantSpecification)

  def reverseIterator: Iterator[Instant] = toReverseLazyList.iterator
}

object CronInstantInterval {

  def apply(
      startValue: LimitValue[Instant],
      endValue: LimitValue[Instant],
      instantSpecification: CronInstantSpecification = CronInstantSpecification.never
  ): CronInstantInterval =
    new CronInstantInterval(startValue, endValue, instantSpecification)

  def everFrom(
      startDate: LimitValue[Instant],
      instantSpecification: CronInstantSpecification = CronInstantSpecification.never
  ): CronInstantInterval =
    inclusive(startDate, Limitless[Instant](), instantSpecification)

  def inclusive(
      start: LimitValue[Instant],
      end: LimitValue[Instant],
      instantSpecification: CronInstantSpecification = CronInstantSpecification.never
  ): CronInstantInterval =
    new CronInstantInterval(start, end, instantSpecification)

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
    new CronInstantInterval(Limit(startDate.toInstant), Limit(endDate.toInstant), instantSpecification)
  }
}
