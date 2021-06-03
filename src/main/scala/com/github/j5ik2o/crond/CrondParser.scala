package com.github.j5ik2o.crond

import fastparse.NoWhitespace._
import fastparse._

trait ExprVisitor[T] {
  def visit(e: Expr): T
}

trait Expr {

  def accept[T](visitor: ExprVisitor[T]): T = {
    visitor.visit(this)
  }
}

case class NoOp() extends Expr

case class ValueExpr(digit: Int) extends Expr

case class LastValue() extends Expr

case class AnyValueExpr() extends Expr

case class PerExpr(digit: Expr, option: Expr) extends Expr

case class RangeExpr(from: Expr, to: Expr, perOtion: Expr) extends Expr

case class ListExpr(exprs: List[Expr]) extends Expr

case class CronExpr(mins: Expr, hours: Expr, days: Expr, months: Expr, dayOfWeeks: Expr) extends Expr

case class CrondParseException(message: String) extends Exception(message)

class CrondParser {
  def SUN[_: P]: P[ValueExpr]  = P(StringIn("SUN", "sun")).!.map(_ => ValueExpr(1))
  def MON[_: P]: P[ValueExpr]  = P(StringIn("MON", "mon")).!.map(_ => ValueExpr(2))
  def TUE[_: P]: P[ValueExpr]  = P(StringIn("TUE", "tue")).!.map(_ => ValueExpr(3))
  def WED[_: P]: P[ValueExpr]  = P(StringIn("WED", "wed")).!.map(_ => ValueExpr(4))
  def THU[_: P]: P[ValueExpr]  = P(StringIn("THU", "tue")).!.map(_ => ValueExpr(5))
  def FRI[_: P]: P[ValueExpr]  = P(StringIn("FRI", "fri")).!.map(_ => ValueExpr(6))
  def SAT[_: P]: P[ValueExpr]  = P(StringIn("SAT", "sat")).!.map(_ => ValueExpr(7))
  def LAST[_: P]: P[LastValue] = P(CharIn("L", "l")).!.map(_ => LastValue())

  def minDigit_00_09[_: P]: P[ValueExpr] = P("0" ~ CharIn("0-9") | CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  def minDigit_10_59[_: P]: P[ValueExpr] = P(CharIn("1-5") ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  def minDigit[_: P]: P[ValueExpr]       = P(minDigit_10_59 | minDigit_00_09)

  def hourDigit_00_09[_: P]: P[ValueExpr] = P("0" ~ CharIn("0-9") | CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  def hourDigit_10_19[_: P]: P[ValueExpr] = P("1" ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  def hourDigit_20_23[_: P]: P[ValueExpr] = P("2" ~ CharIn("0-3")).!.map(s => ValueExpr(s.toInt))
  def hourDigit[_: P]: P[ValueExpr]       = P(hourDigit_20_23 | hourDigit_10_19 | hourDigit_00_09)

  def dayDigit_01_09[_: P]: P[ValueExpr] = P("0" ~ CharIn("1-9") | CharIn("1-9")).!.map(s => ValueExpr(s.toInt))
  def dayDigit_10_29[_: P]: P[ValueExpr] = P(CharIn("1-2") ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  def dayDigit_30_31[_: P]: P[ValueExpr] = P("3" ~ CharIn("0-1")).!.map(s => ValueExpr(s.toInt))
  def dayDigit[_: P]: P[ValueExpr]       = P(dayDigit_30_31 | dayDigit_10_29 | dayDigit_01_09)

  def monthDigit_00_09[_: P]: P[ValueExpr] = P("0" ~ CharIn("1-9") | CharIn("1-9")).!.map(s => ValueExpr(s.toInt))
  def monthDigit_10_12[_: P]: P[ValueExpr] = P("1" ~ CharIn("0-2")).!.map(s => ValueExpr(s.toInt))
  def monthDigit[_: P]: P[ValueExpr]       = P(monthDigit_10_12 | monthDigit_00_09)

  def dayOfWeekDigit[_: P]: P[ValueExpr] = P(CharIn("1-7")).!.map(s => ValueExpr(s.toInt))
  def dayOfWeekText[_: P]: P[Expr]       = SUN | MON | TUE | WED | THU | FRI | SAT | LAST
  def asterisk[_: P]: P[AnyValueExpr]    = P("*").!.map(_ => AnyValueExpr())

  def per[_: P](digit: => P[Expr]): P[Expr] = P("/" ~ digit)

  def asteriskPer[_: P](digit: => P[Expr]): P[PerExpr] = P(asterisk ~ per(digit)).map { case (d, op) =>
    PerExpr(d, op)
  }

  def rangePer[_: P](digit: => P[Expr]): P[Expr] = P(per(digit).?).map {
    case None    => NoOp()
    case Some(d) => d
  }

  def range[_: P](digit: => P[Expr]): P[RangeExpr] = P(digit ~ "-" ~ digit ~ rangePer(digit)).map {
    case (from, to, per) =>
      RangeExpr(from, to, per)
  }

  def list[_: P](digit: => P[Expr]): P[Expr] = P(digit.rep(sep = ",")).map(_.toList).map {
    case x :: Nil => x
    case l        => ListExpr(l)
  }

  def digitInstruction[_: P](digit: => P[Expr]): P[Expr] = P(
    asteriskPer(digit) | asterisk | list(range(digit) | digit)
  )

  def instruction[_: P]: P[CronExpr] = P(
    digitInstruction(minDigit) ~ " " ~ digitInstruction(hourDigit) ~ " " ~
    digitInstruction(dayDigit) ~ " " ~ digitInstruction(monthDigit) ~
    " " ~ digitInstruction(dayOfWeekText | dayOfWeekDigit)
  ).map { case (mins, hours, days, months, dayOfWeeks) =>
    CronExpr(mins, hours, days, months, dayOfWeeks)
  }
}
