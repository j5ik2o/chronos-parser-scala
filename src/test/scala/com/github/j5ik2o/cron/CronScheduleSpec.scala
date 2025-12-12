package com.github.j5ik2o.cron

import org.scalatest.funsuite.AnyFunSuite

import java.time.{Duration, ZoneId, ZonedDateTime}

class CronScheduleSpec extends AnyFunSuite {

  test("upcoming") {
    val cronExpression = "*/3 * * * *"
    val cronSchedule = CronSchedule(cronExpression, ZoneId.systemDefault())
    val zoneId = ZoneId.systemDefault()
    val now = ZonedDateTime.of(2021, 1, 1, 1, 3, 0, 0, zoneId)
    println(s"n = $now")
    val startInstant = now.toInstant
    val upcomingList = cronSchedule.upcoming(startInstant).take(5)
    upcomingList.zipWithIndex.foreach { case (instant, index) =>
      assert(instant == startInstant.plus(Duration.ofMinutes(3 * index)))
    }
    upcomingList.zipWithIndex.foreach { case (i, index) =>
      val zdt = ZonedDateTime.ofInstant(i, zoneId)
      println(s"$index = $zdt")
    }
  }

}
