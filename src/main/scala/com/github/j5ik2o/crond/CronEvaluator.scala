package com.github.j5ik2o.crond

import com.github.j5ik2o.crond.CronEvaluator.Mapping

import java.time.temporal.TemporalAdjusters
import java.time.{ Instant, LocalDateTime, ZoneId }
import java.util.{ Calendar, TimeZone }

object CronEvaluator {

  val Mapping = Map(
    java.time.DayOfWeek.SUNDAY    -> Calendar.SUNDAY,
    java.time.DayOfWeek.MONDAY    -> Calendar.MONDAY,
    java.time.DayOfWeek.TUESDAY   -> Calendar.TUESDAY,
    java.time.DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY,
    java.time.DayOfWeek.THURSDAY  -> Calendar.THURSDAY,
    java.time.DayOfWeek.FRIDAY    -> Calendar.FRIDAY,
    java.time.DayOfWeek.SATURDAY  -> Calendar.SATURDAY
  )

}

class CronEvaluator(instant: Instant, zoneId: ZoneId) extends ExprVisitor[Boolean] {

  override def visit(e: Expr): Boolean = e match {
    case CronExpr(mins, hours, days, months, dayOfWeeks) => {

      val ldt       = LocalDateTime.ofInstant(instant, zoneId)
      val min       = ldt.getMinute
      val hour      = ldt.getHour
      val day       = ldt.getDayOfMonth
      val month     = ldt.getMonthValue
      val monthMax  = ldt.getMonth.maxLength()
      val dayOfWeek = Mapping(ldt.getDayOfWeek)

      val minMax       = 59
      val hourMax      = 23
      val dayMax       = ldt.`with`(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth
      val dayOfWeekMax = 7

      val m  = mins.accept(ExpressionEvaluator(min, minMax))
      val h  = hours.accept(ExpressionEvaluator(hour, hourMax))
      val d  = days.accept(ExpressionEvaluator(day, dayMax))
      val M  = months.accept(ExpressionEvaluator(month, monthMax))
      val dw = dayOfWeeks.accept(ExpressionEvaluator(dayOfWeek, dayOfWeekMax))

      m && h && d && M && dw
    }
    case _ => false
  }

  case class ExpressionEvaluator(now: Int, max: Int) extends ExprVisitor[Boolean] {

    //println("now = %d, max = %d".format(now, max))
    def visit(e: Expr): Boolean = e match {
      case AnyValueExpr()            => true
      case LastValue() if now == max => true
      case ValueExpr(n) if now == n  => true
      case ListExpr(list)            => list.exists(_.accept(this))
      case RangeExpr(ValueExpr(start), ValueExpr(end), op) =>
        op match {
          case NoOp() if start <= now && now <= end => true
          case ValueExpr(per)                       => (start to end by per).exists(_ == now)
          case _                                    => false
        }
      case PerExpr(AnyValueExpr(), ValueExpr(per)) => (0 until max by per).exists(_ == now)
      case _                                       => false
    }
  }
}
