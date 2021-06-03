package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.ast.CronExpr
import com.github.j5ik2o.intervals.Limit

import java.time.{ Instant, ZoneId }

class CronSchedule(cronExpression: String, zoneId: ZoneId) {
  val expr: CronExpr = new CronParser().parse(cronExpression)

  def instantInterval(start: Instant): CronInstantInterval =
    CronInstantInterval.everFrom(Limit(start), CronInstantSpecification.of(expr, zoneId))

  def getInstantAfter(base: Instant, numberOfMinutes: Int): Option[Instant] =
    instantInterval(base).getInstantAfter(base, numberOfMinutes)

  def upcoming(start: Instant): LazyList[Instant] = instantInterval(start).toLazyList

}
