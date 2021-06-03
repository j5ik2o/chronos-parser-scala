package com.github.j5ik2o.cron

import com.github.j5ik2o.cron.ast.CronExpr

import java.time.ZoneId

object CronInstantSpecification {

  lazy val never: CronInstantSpecification = { _ => false }

  def of(expr: CronExpr, zoneId: ZoneId): CronInstantSpecification = { instant =>
    CronEvaluator(instant, zoneId).visit(expr)
  }

}
