package com.github.j5ik2o.crond

import com.github.j5ik2o.intervals.{ Interval, Limit, LimitValue, Limitless }
import sun.util.calendar.CalendarDate

import java.time.{ Duration, Instant }

class InstantInterval(startValue: LimitValue[Instant], endValue: LimitValue[Instant], interval: Duration)
    extends Interval[Instant](startValue, true, endValue, true)
    with Serializable {

  def createStream(startValue: LimitValue[Instant]): LazyList[Instant] = {
    require(hasLowerLimit)
    LazyList.cons(startValue.toValue, createStream(startValue.toValue.plus(interval))).takeWhile { v =>
      endValue match {
        case _: Limitless[Instant] => true
        case Limit(end)            => !v.isAfter(end)
      }
    }
  }

  lazy val timesIterator: Iterator[Instant] = {
    if (!hasLowerLimit) {
      throw new IllegalStateException
    }

    val start = lowerLimit
    val end   = upperLimit

    new Iterator[Instant] {

      var _next: LimitValue[Instant] = start

      override def hasNext: Boolean = {
        end match {
          case _: Limitless[Instant] => true
          case Limit(v)              => !_next.toValue.isAfter(v)
        }
      }

      override def next: Instant = {
        if (!hasNext) {
          throw new NoSuchElementException
        }
        val current = _next
        _next = Limit(_next.toValue.plus(interval))
        current.toValue
      }
    }
  }

  lazy val timesInReverseIterator: Iterator[Instant] = {
    if (!hasUpperLimit) {
      throw new IllegalStateException
    }

    val start = upperLimit
    val end   = lowerLimit

    new Iterator[Instant] {

      var _next: LimitValue[Instant] = start

      override def hasNext: Boolean = {
        end match {
          case _: Limitless[Instant] => true
          case Limit(v)              => !_next.toValue.isBefore(v)
        }
      }

      override def next: Instant = {
        if (!hasNext) {
          throw new NoSuchElementException
        }
        val current = _next
        _next = Limit(_next.toValue.minus(interval))
        current.toValue
      }
    }
  }

}

object TimePointInterval {

  def apply(startValue: LimitValue[Instant], endValue: LimitValue[Instant], interval: Duration): InstantInterval =
    new InstantInterval(startValue, endValue, interval)

  def everFrom(startDate: LimitValue[Instant], interval: Duration): InstantInterval =
    inclusive(startDate, Limitless[Instant](), interval)

  def inclusive(start: LimitValue[Instant], end: LimitValue[Instant], interval: Duration): InstantInterval =
    new InstantInterval(start, end, interval)

  def inclusive(
      startCalendarDate: CalendarDate,
      startTimeOfDay: TimeOfDay,
      endCalendarDate: CalendarDate,
      endTimeOfDay: TimeOfDay,
      interval: Duration
  ): InstantInterval = {
    Instant.
    val startDate = TimePoint.from(startCalendarDate, startTimeOfDay)
    val endDate   = TimePoint.from(endCalendarDate, endTimeOfDay)
    new InstantInterval(Limit(startDate), Limit(endDate), interval)
  }
}
