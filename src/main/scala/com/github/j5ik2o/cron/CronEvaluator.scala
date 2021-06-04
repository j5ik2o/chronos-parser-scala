package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.CronEvaluator._
import com.github.j5ik2o.cron.ast._

import java.time.temporal.TemporalAdjusters
import java.time.{ Instant, LocalDateTime, ZoneId }
import java.util.Calendar
import java.time.DayOfWeek

object CronEvaluator {

  final val Mapping: Map[DayOfWeek, Int] = Map(
    java.time.DayOfWeek.SUNDAY    -> Calendar.SUNDAY,
    java.time.DayOfWeek.MONDAY    -> Calendar.MONDAY,
    java.time.DayOfWeek.TUESDAY   -> Calendar.TUESDAY,
    java.time.DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY,
    java.time.DayOfWeek.THURSDAY  -> Calendar.THURSDAY,
    java.time.DayOfWeek.FRIDAY    -> Calendar.FRIDAY,
    java.time.DayOfWeek.SATURDAY  -> Calendar.SATURDAY
  )

  final val MinMax       = 59
  final val HourMax      = 23
  final val DayOfWeekMax = 7

  def apply(instant: Instant, zoneId: ZoneId) = new CronEvaluator(instant, zoneId)
}

class CronEvaluator(instant: Instant, zoneId: ZoneId) extends ExprVisitor[Boolean] {

  private val ldt       = LocalDateTime.ofInstant(instant, zoneId)
  private val dayMax    = ldt.`with`(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth
  private val min       = ldt.getMinute
  private val hour      = ldt.getHour
  private val day       = ldt.getDayOfMonth
  private val month     = ldt.getMonthValue
  private val monthMax  = ldt.getMonth.maxLength()
  private val dayOfWeek = Mapping(ldt.getDayOfWeek)

  override def visit(e: Expr): Boolean = e match {
    case CronExpr(mins, hours, days, months, dayOfWeeks) => {

      val m  = mins.accept(ExpressionEvaluator(min, MinMax))
      val h  = hours.accept(ExpressionEvaluator(hour, HourMax))
      val d  = days.accept(ExpressionEvaluator(day, dayMax))
      val M  = months.accept(ExpressionEvaluator(month, monthMax))
      val dw = dayOfWeeks.accept(ExpressionEvaluator(dayOfWeek, DayOfWeekMax))

      m && h && d && M && dw
    }
    case _ => false
  }

  case class ExpressionEvaluator(now: Int, max: Int) extends ExprVisitor[Boolean] {

    //println("now = %d, max = %d".format(now, max))
    def visit(e: Expr): Boolean = e match {
      case AnyValueExpr()                => true
      case LastValueExpr() if now == max => true
      case ValueExpr(n) if now == n      => true
      case ListExpr(list)                => list.exists(_.accept(this))
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
