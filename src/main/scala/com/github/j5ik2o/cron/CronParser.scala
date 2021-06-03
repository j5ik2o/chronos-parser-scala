package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.ast._
import fastparse.NoWhitespace._
import fastparse._

object CronParser {

  def parse(source: String): CronExpr = fastparse.parse(source, instruction(_)) match {
    case Parsed.Success(result, _) => result
    case failure: Parsed.Failure   => throw new CronParseException(failure.msg)
  }

  private[cron] def SUN[_: P]: P[ValueExpr]      = P(StringIn("SUN", "sun")).!.map(_ => ValueExpr(1))
  private[cron] def MON[_: P]: P[ValueExpr]      = P(StringIn("MON", "mon")).!.map(_ => ValueExpr(2))
  private[cron] def TUE[_: P]: P[ValueExpr]      = P(StringIn("TUE", "tue")).!.map(_ => ValueExpr(3))
  private[cron] def WED[_: P]: P[ValueExpr]      = P(StringIn("WED", "wed")).!.map(_ => ValueExpr(4))
  private[cron] def THU[_: P]: P[ValueExpr]      = P(StringIn("THU", "tue")).!.map(_ => ValueExpr(5))
  private[cron] def FRI[_: P]: P[ValueExpr]      = P(StringIn("FRI", "fri")).!.map(_ => ValueExpr(6))
  private[cron] def SAT[_: P]: P[ValueExpr]      = P(StringIn("SAT", "sat")).!.map(_ => ValueExpr(7))
  private[cron] def LAST[_: P]: P[LastValueExpr] = P(CharIn("L", "l")).!.map(_ => LastValueExpr())

  private[cron] def minDigit_00_09[_: P]: P[ValueExpr] =
    P("0" ~ CharIn("0-9") | CharIn("0-9")).!.map(s => ValueExpr(s.toInt))

  private[cron] def minDigit_10_59[_: P]: P[ValueExpr] =
    P(CharIn("1-5") ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def minDigit[_: P]: P[ValueExpr] = P(minDigit_10_59 | minDigit_00_09)

  private[cron] def hourDigit_00_09[_: P]: P[ValueExpr] =
    P("0" ~ CharIn("0-9") | CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def hourDigit_10_19[_: P]: P[ValueExpr] = P("1" ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def hourDigit_20_23[_: P]: P[ValueExpr] = P("2" ~ CharIn("0-3")).!.map(s => ValueExpr(s.toInt))
  private[cron] def hourDigit[_: P]: P[ValueExpr]       = P(hourDigit_20_23 | hourDigit_10_19 | hourDigit_00_09)

  private[cron] def dayDigit_01_09[_: P]: P[ValueExpr] =
    P("0" ~ CharIn("1-9") | CharIn("1-9")).!.map(s => ValueExpr(s.toInt))

  private[cron] def dayDigit_10_29[_: P]: P[ValueExpr] =
    P(CharIn("1-2") ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def dayDigit_30_31[_: P]: P[ValueExpr] = P("3" ~ CharIn("0-1")).!.map(s => ValueExpr(s.toInt))
  private[cron] def dayDigit[_: P]: P[ValueExpr]       = P(dayDigit_30_31 | dayDigit_10_29 | dayDigit_01_09)

  private[cron] def monthDigit_00_09[_: P]: P[ValueExpr] =
    P("0" ~ CharIn("1-9") | CharIn("1-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def monthDigit_10_12[_: P]: P[ValueExpr] = P("1" ~ CharIn("0-2")).!.map(s => ValueExpr(s.toInt))
  private[cron] def monthDigit[_: P]: P[ValueExpr]       = P(monthDigit_10_12 | monthDigit_00_09)

  private[cron] def dayOfWeekDigit[_: P]: P[ValueExpr] = P(CharIn("1-7")).!.map(s => ValueExpr(s.toInt))
  private[cron] def dayOfWeekText[_: P]: P[Expr]       = SUN | MON | TUE | WED | THU | FRI | SAT | LAST
  private[cron] def asterisk[_: P]: P[AnyValueExpr]    = P("*").!.map(_ => AnyValueExpr())

  private[cron] def per[_: P](digit: => P[Expr]): P[Expr] = P("/" ~ digit)

  private[cron] def asteriskPer[_: P](digit: => P[Expr]): P[PerExpr] = P(asterisk ~ per(digit)).map { case (d, op) =>
    PerExpr(d, op)
  }

  private[cron] def rangePer[_: P](digit: => P[Expr]): P[Expr] = P(per(digit).?).map {
    case None    => NoOp()
    case Some(d) => d
  }

  private[cron] def range[_: P](digit: => P[Expr]): P[RangeExpr] = P(digit ~ "-" ~ digit ~ rangePer(digit)).map {
    case (from, to, per) =>
      RangeExpr(from, to, per)
  }

  private[cron] def list[_: P](digit: => P[Expr]): P[Expr] = P(digit.rep(sep = ",")).map(_.toList).map {
    case x :: Nil => x
    case l        => ListExpr(l)
  }

  private[cron] def digitInstruction[_: P](digit: => P[Expr]): P[Expr] = P(
    asteriskPer(digit) | asterisk | list(range(digit) | digit)
  )

  private[cron] def instruction[_: P]: P[CronExpr] = P(
    digitInstruction(minDigit) ~ " " ~ digitInstruction(hourDigit) ~ " " ~
    digitInstruction(dayDigit) ~ " " ~ digitInstruction(monthDigit) ~
    " " ~ digitInstruction(dayOfWeekText | dayOfWeekDigit)
  ).map { case (mins, hours, days, months, dayOfWeeks) =>
    CronExpr(mins, hours, days, months, dayOfWeeks)
  }
}
