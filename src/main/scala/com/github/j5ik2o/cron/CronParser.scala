package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.ast._
import fastparse.NoWhitespace._
import fastparse._

object CronParser {

  def parse(source: String): CronExpr = fastparse.parse(source, instruction(_)) match {
    case Parsed.Success(result, _) => result
    case failure: Parsed.Failure => throw new CronParseException(failure.msg)
  }

  private[cron] def SUN[$ : P]: P[ValueExpr] = P(StringIn("SUN", "sun")).!.map(_ => ValueExpr(1))
  private[cron] def MON[$ : P]: P[ValueExpr] = P(StringIn("MON", "mon")).!.map(_ => ValueExpr(2))
  private[cron] def TUE[$ : P]: P[ValueExpr] = P(StringIn("TUE", "tue")).!.map(_ => ValueExpr(3))
  private[cron] def WED[$ : P]: P[ValueExpr] = P(StringIn("WED", "wed")).!.map(_ => ValueExpr(4))
  private[cron] def THU[$ : P]: P[ValueExpr] = P(StringIn("THU", "tue")).!.map(_ => ValueExpr(5))
  private[cron] def FRI[$ : P]: P[ValueExpr] = P(StringIn("FRI", "fri")).!.map(_ => ValueExpr(6))
  private[cron] def SAT[$ : P]: P[ValueExpr] = P(StringIn("SAT", "sat")).!.map(_ => ValueExpr(7))
  private[cron] def LAST[$ : P]: P[LastValueExpr] = P(CharIn("L", "l")).!.map(_ => LastValueExpr())

  private[cron] def minDigit_00_09[$ : P]: P[ValueExpr] =
    P("0" ~ CharIn("0-9") | CharIn("0-9")).!.map(s => ValueExpr(s.toInt))

  private[cron] def minDigit_10_59[$ : P]: P[ValueExpr] =
    P(CharIn("1-5") ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def minDigit[$ : P]: P[ValueExpr] = P(minDigit_10_59 | minDigit_00_09)

  private[cron] def hourDigit_00_09[$ : P]: P[ValueExpr] =
    P("0" ~ CharIn("0-9") | CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def hourDigit_10_19[$ : P]: P[ValueExpr] =
    P("1" ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def hourDigit_20_23[$ : P]: P[ValueExpr] =
    P("2" ~ CharIn("0-3")).!.map(s => ValueExpr(s.toInt))
  private[cron] def hourDigit[$ : P]: P[ValueExpr] = P(
    hourDigit_20_23 | hourDigit_10_19 | hourDigit_00_09)

  private[cron] def dayDigit_01_09[$ : P]: P[ValueExpr] =
    P("0" ~ CharIn("1-9") | CharIn("1-9")).!.map(s => ValueExpr(s.toInt))

  private[cron] def dayDigit_10_29[$ : P]: P[ValueExpr] =
    P(CharIn("1-2") ~ CharIn("0-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def dayDigit_30_31[$ : P]: P[ValueExpr] =
    P("3" ~ CharIn("0-1")).!.map(s => ValueExpr(s.toInt))
  private[cron] def dayDigit[$ : P]: P[ValueExpr] = P(
    dayDigit_30_31 | dayDigit_10_29 | dayDigit_01_09)

  private[cron] def monthDigit_00_09[$ : P]: P[ValueExpr] =
    P("0" ~ CharIn("1-9") | CharIn("1-9")).!.map(s => ValueExpr(s.toInt))
  private[cron] def monthDigit_10_12[$ : P]: P[ValueExpr] =
    P("1" ~ CharIn("0-2")).!.map(s => ValueExpr(s.toInt))
  private[cron] def monthDigit[$ : P]: P[ValueExpr] = P(monthDigit_10_12 | monthDigit_00_09)

  private[cron] def dayOfWeekDigit[$ : P]: P[ValueExpr] =
    P(CharIn("1-7")).!.map(s => ValueExpr(s.toInt))
  private[cron] def dayOfWeekText[$ : P]: P[Expr] = SUN | MON | TUE | WED | THU | FRI | SAT | LAST
  private[cron] def asterisk[$ : P]: P[AnyValueExpr] = P("*").!.map(_ => AnyValueExpr())

  private[cron] def per[$ : P](digit: => P[Expr]): P[Expr] = P("/" ~ digit)

  private[cron] def asteriskPer[$ : P](digit: => P[Expr]): P[PerExpr] =
    P(asterisk ~ per(digit)).map { case (d, op) =>
      PerExpr(d, op)
    }

  private[cron] def rangePer[$ : P](digit: => P[Expr]): P[Expr] = P(per(digit).?).map {
    case None => NoOp()
    case Some(d) => d
  }

  private[cron] def range[$ : P](digit: => P[Expr]): P[RangeExpr] =
    P(digit ~ "-" ~ digit ~ rangePer(digit)).map { case (from, to, per) =>
      RangeExpr(from, to, per)
    }

  private[cron] def list[$ : P](digit: => P[Expr]): P[Expr] =
    P(digit.rep(sep = ",")).map(_.toList).map {
      case x :: Nil => x
      case l => ListExpr(l)
    }

  private[cron] def digitInstruction[$ : P](digit: => P[Expr]): P[Expr] = P(
    asteriskPer(digit) | asterisk | list(range(digit) | digit)
  )

  private[cron] def instruction[$ : P]: P[CronExpr] = P(
    digitInstruction(minDigit) ~ " " ~ digitInstruction(hourDigit) ~ " " ~
      digitInstruction(dayDigit) ~ " " ~ digitInstruction(monthDigit) ~
      " " ~ digitInstruction(dayOfWeekText | dayOfWeekDigit)
  ).map { case (mins, hours, days, months, dayOfWeeks) =>
    CronExpr(mins, hours, days, months, dayOfWeeks)
  }
}
