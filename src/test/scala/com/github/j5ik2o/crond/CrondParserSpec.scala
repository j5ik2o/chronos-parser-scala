package com.github.j5ik2o.crond

import fastparse._, NoWhitespace._
import org.scalatest.funsuite.AnyFunSuite

class CrondParserSpec extends AnyFunSuite {
  val crondParser = new CrondParser()

  test("SUN") {
    val Parsed.Success(value, successIndex) = parse("SUN", crondParser.SUN(_))
    assert(value == ValueExpr(1), successIndex == 1)
  }

  test("MON") {
    val Parsed.Success(value, successIndex) = parse("MON", crondParser.MON(_))
    assert(value == ValueExpr(2), successIndex == 1)
  }

  test("TUE") {
    val Parsed.Success(value, successIndex) = parse("TUE", crondParser.TUE(_))
    assert(value == ValueExpr(3), successIndex == 1)
  }

  test("WED") {
    val Parsed.Success(value, successIndex) = parse("WED", crondParser.TUE(_))
    assert(value == ValueExpr(4), successIndex == 1)
  }

  test("THU") {
    val Parsed.Success(value, successIndex) = parse("THU", crondParser.THU(_))
    assert(value == ValueExpr(5), successIndex == 1)
  }

  test("FRI") {
    val Parsed.Success(value, successIndex) = parse("FRI", crondParser.FRI(_))
    assert(value == ValueExpr(6), successIndex == 1)
  }

  test("SAT") {
    val Parsed.Success(value, successIndex) = parse("SAT", crondParser.SAT(_))
    assert(value == ValueExpr(7), successIndex == 1)
  }

  test("LAST") {
    val Parsed.Success(value, successIndex) = parse("LAST", crondParser.LAST(_))
    assert(value == LastValue(), successIndex == 1)
  }

  test("dayOfWeekText") {
    for ((s, n) <- Seq("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT", "LAST").zipWithIndex) {
      val Parsed.Success(value, successIndex) = parse(s, crondParser.dayOfWeekText(_))
      if (n == 7)
        assert(value == LastValue(), successIndex == 1)
      else
        assert(value == ValueExpr(n + 1), successIndex == 1)

    }
  }

  test("dayOfWeekDigit") {
    for (n <- 1 to 7) {
      val Parsed.Success(value, successIndex) = parse(n.toString, crondParser.dayOfWeekDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
  }

  test("dayDigit") {
    for (n <- 1 to 31) {
      val Parsed.Success(value, successIndex) = parse(n.toString, crondParser.dayDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
    for (n <- 1 to 31) {
      val s                                   = "%02d".format(n)
      val Parsed.Success(value, successIndex) = parse(s, crondParser.dayDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
  }

  test("monthDigit") {
    for (n <- 1 to 12) {
      val Parsed.Success(value, successIndex) = parse(n.toString, crondParser.monthDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
    for (n <- 1 to 12) {
      val s                                   = "%02d".format(n)
      val Parsed.Success(value, successIndex) = parse(s, crondParser.monthDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
  }

  test("hourDigit") {
    for (n <- 0 to 23) {
      val Parsed.Success(value, successIndex) = parse(n.toString, crondParser.hourDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
    for (n <- 0 to 23) {
      val s                                   = "%02d".format(n)
      val Parsed.Success(value, successIndex) = parse(s, crondParser.hourDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
  }

  test("minDigit") {
    for (n <- 0 to 59) {
      val Parsed.Success(value, successIndex) = parse(n.toString, crondParser.minDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
    for (n <- 0 to 59) {
      val s                                   = "%02d".format(n)
      val Parsed.Success(value, successIndex) = parse(s, crondParser.minDigit(_))
      assert(value == ValueExpr(n), successIndex == 1)
    }
  }

  test("asterisk") {
    val s                                   = "*"
    val Parsed.Success(value, successIndex) = parse(s, crondParser.asterisk(_))
    assert(value == AnyValueExpr(), successIndex == 1)
  }

  test("per") {
    def p[_: P]: P[Expr]                    = crondParser.per(crondParser.minDigit)
    val s                                   = "/2"
    val Parsed.Success(value, successIndex) = parse(s, p(_))
    assert(value == ValueExpr(2), successIndex == 1)
  }

  test("asteriskPer") {
    def p[_: P]: P[PerExpr]                 = crondParser.asteriskPer(crondParser.minDigit)
    val s                                   = "*/2"
    val Parsed.Success(value, successIndex) = parse(s, p(_))
    assert(value == PerExpr(AnyValueExpr(), ValueExpr(2)), successIndex == 1)
  }

  test("rangePer") {
    def p[_: P]: P[Expr]                      = crondParser.rangePer(crondParser.minDigit)
    val s1                                    = "/2"
    val Parsed.Success(value1, successIndex1) = parse(s1, p(_))
    assert(value1 == ValueExpr(2), successIndex1 == 1)
    val s2                                    = "/"
    val Parsed.Success(value2, successIndex2) = parse(s2, p(_))
    assert(value2 == NoOp(), successIndex2 == 1)
  }

  test("range") {
    def p[_: P]: P[RangeExpr]               = crondParser.range(crondParser.minDigit)
    val s                                   = "1-10/2"
    val Parsed.Success(value, successIndex) = parse(s, p(_))
    assert(value == RangeExpr(ValueExpr(1), ValueExpr(10), ValueExpr(2)), successIndex == 1)
  }

  test("list") {
    def p[_: P]: P[Expr]                    = crondParser.list(crondParser.minDigit)
    val s                                   = "1,2,3"
    val Parsed.Success(value, successIndex) = parse(s, p(_))
    assert(value == ListExpr(List(ValueExpr(1), ValueExpr(2), ValueExpr(3))), successIndex == 1)
  }

  test("digitInstruction") {
    def p[_: P]: P[Expr]                      = crondParser.digitInstruction(crondParser.minDigit)
    val s1                                    = "1,2,5,9"
    val Parsed.Success(value1, successIndex1) = parse(s1, p(_))
    assert(value1 == ListExpr(List(ValueExpr(1), ValueExpr(2), ValueExpr(5), ValueExpr(9))), successIndex1 == 1)
    val s2                                    = "0-4,8-12"
    val Parsed.Success(value2, successIndex2) = parse(s2, p(_))
    assert(
      value2 == ListExpr(
        List(RangeExpr(ValueExpr(0), ValueExpr(4), NoOp()), RangeExpr(ValueExpr(8), ValueExpr(12), NoOp()))
      ),
      successIndex2 == 1
    )
  }

  test("instruction") {
    {
      val s                                   = "1 1 1 1 1"
      val Parsed.Success(value, successIndex) = parse(s, crondParser.instruction(_))
      assert(
        value == CronExpr(
          mins = ValueExpr(1),
          hours = ValueExpr(1),
          days = ValueExpr(1),
          months = ValueExpr(1),
          dayOfWeeks = ValueExpr(1)
        ),
        successIndex == 1
      )
    }
    {
      val s                                   = "10 14 * * 1"
      val Parsed.Success(value, successIndex) = parse(s, crondParser.instruction(_))
      assert(
        value == CronExpr(ValueExpr(10), ValueExpr(14), AnyValueExpr(), AnyValueExpr(), ValueExpr(1)),
        successIndex == 1
      )
    }
    {
      val s                                   = "0 0 * * 1-5"
      val Parsed.Success(value, successIndex) = parse(s, crondParser.instruction(_))
      assert(
        value == CronExpr(
          ValueExpr(0),
          ValueExpr(0),
          AnyValueExpr(),
          AnyValueExpr(),
          RangeExpr(ValueExpr(1), ValueExpr(5), NoOp())
        ),
        successIndex == 1
      )
    }
    {
      val s                                   = "0 0 1,15 * *"
      val Parsed.Success(value, successIndex) = parse(s, crondParser.instruction(_))
      assert(
        value == CronExpr(
          ValueExpr(0),
          ValueExpr(0),
          ListExpr(List(ValueExpr(1), ValueExpr(15))),
          AnyValueExpr(),
          AnyValueExpr()
        ),
        successIndex == 1
      )
    }
    {
      val s                                   = "32 18 17,21,29 11 MON,WED"
      val Parsed.Success(value, successIndex) = parse(s, crondParser.instruction(_))
      assert(
        value == CronExpr(
          ValueExpr(32),
          ValueExpr(18),
          ListExpr(List(ValueExpr(17), ValueExpr(21), ValueExpr(29))),
          ValueExpr(11),
          ListExpr(List(ValueExpr(2), ValueExpr(4)))
        ),
        successIndex == 1
      )
    }
  }

}
