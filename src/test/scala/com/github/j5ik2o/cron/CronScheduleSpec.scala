package com.github.j5ik2o.cron

import org.scalatest.funsuite.AnyFunSuite

import java.time.{ Duration, Instant, ZoneId }

class CronScheduleSpec extends AnyFunSuite {

  test("upcoming") {
    val cronExpression = "*/1 * * * *"
    val cronSchedule   = CronSchedule(cronExpression, ZoneId.systemDefault())
    val start          = Instant.now()
    val actuals        = cronSchedule.upcoming(start).take(2).toList
    assert(actuals(0) == start)
    assert(actuals(1) == start.plus(Duration.ofMinutes(1)))
    actuals.foreach(println)
  }

}
