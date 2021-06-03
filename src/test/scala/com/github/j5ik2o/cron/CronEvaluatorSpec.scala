package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.ast._
import com.github.j5ik2o.intervals.Limit
import org.scalatest.funsuite.AnyFunSuite

import java.time.{ Duration, Instant, LocalDateTime, ZoneId, ZoneOffset }

class CronEvaluatorSpec extends AnyFunSuite {
  val zoneId = ZoneId.systemDefault()

  ignore("example") {
    val cronExpression = "*/1 * * * *"
    val expr           = new CronParser().parse(cronExpression)

    val start = Instant.now().plus(Duration.ofSeconds(3))
    val crondInstants: LazyList[Instant] =
      CronInstantInterval.everFrom(Limit(start), CronInstantSpecification.of(expr, zoneId)).toLazyList.take(2)

    def loop(crondInstants: LazyList[Instant]): Unit = {
      crondInstants match {
        case l if l.isEmpty => ()
        case l @ h #:: t =>
          val now = Instant.now()
          println(s"h = $h, now = $now")
          if (now.isAfter(h)) {
            println("trigger!!!")
            Thread.sleep(5000)
            loop(t)
          } else {
            Thread.sleep(5000)
            loop(l)
          }
      }
    }

    loop(crondInstants)

  }

  test("単一の分を評価できること") {
    val ast = CronExpr(
      mins = ValueExpr(1),
      hours = ValueExpr(1),
      days = ValueExpr(1),
      months = ValueExpr(1),
      dayOfWeeks = AnyValueExpr()
    )
    val instant1  = LocalDateTime.of(2011, 1, 1, 1, 1).toInstant(ZoneOffset.ofHours(9))
    val evaluator = new CronEvaluator(instant1, zoneId)
    assert(ast.accept(evaluator))

    val ast2 = CronExpr(
      mins = ValueExpr(0),
      hours = ValueExpr(1),
      days = ValueExpr(1),
      months = ValueExpr(1),
      dayOfWeeks = AnyValueExpr()
    )
    val instant2   = LocalDateTime.of(2011, 1, 1, 1, 1).toInstant(ZoneOffset.ofHours(9))
    val evaluator2 = new CronEvaluator(instant2, zoneId)
    assert(!ast2.accept(evaluator2))

  }

  test("複数の分を評価できること") {
    val ast = CronExpr(
      mins = ListExpr(List(ValueExpr(1), ValueExpr(2), ValueExpr(3))),
      hours = ValueExpr(1),
      days = ValueExpr(1),
      months = ValueExpr(1),
      dayOfWeeks = AnyValueExpr()
    )
    val instant1 = LocalDateTime.of(2011, 1, 1, 1, 1).toInstant(ZoneOffset.ofHours(9))
    val instant2 = LocalDateTime.of(2011, 1, 1, 1, 2).toInstant(ZoneOffset.ofHours(9))
    val instant3 = LocalDateTime.of(2011, 1, 1, 1, 3).toInstant(ZoneOffset.ofHours(9))

    assert(ast.accept(new CronEvaluator(instant1, zoneId)))
    assert(ast.accept(new CronEvaluator(instant2, zoneId)))
    assert(ast.accept(new CronEvaluator(instant3, zoneId)))
  }

  test("範囲の分を評価できること") {
    val ast =
      CronExpr(
        mins = RangeExpr(ValueExpr(1), ValueExpr(3), NoOp()),
        hours = ValueExpr(1),
        days = ValueExpr(1),
        months = ValueExpr(1),
        dayOfWeeks = AnyValueExpr()
      )

    val instant1 = LocalDateTime.of(2011, 1, 1, 1, 1).toInstant(ZoneOffset.ofHours(9))
    val instant2 = LocalDateTime.of(2011, 1, 1, 1, 2).toInstant(ZoneOffset.ofHours(9))
    val instant3 = LocalDateTime.of(2011, 1, 1, 1, 3).toInstant(ZoneOffset.ofHours(9))

    assert(ast.accept(new CronEvaluator(instant1, zoneId)))
    assert(ast.accept(new CronEvaluator(instant2, zoneId)))
    assert(ast.accept(new CronEvaluator(instant3, zoneId)))
  }

  test("範囲(分割)の分を評価できること") {
    val ast = CronExpr(
      mins = RangeExpr(ValueExpr(1), ValueExpr(4), ValueExpr(2)),
      hours = ValueExpr(1),
      days = ValueExpr(1),
      months = ValueExpr(1),
      dayOfWeeks = AnyValueExpr()
    )

    val instant1 = LocalDateTime.of(2011, 1, 1, 1, 1).toInstant(ZoneOffset.ofHours(9))
    val instant2 = LocalDateTime.of(2011, 1, 1, 1, 3).toInstant(ZoneOffset.ofHours(9))

    assert(ast.accept(new CronEvaluator(instant1, zoneId)))
    assert(ast.accept(new CronEvaluator(instant2, zoneId)))
  }
}
