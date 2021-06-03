package com.github.j5ik2o

import java.time.Instant

package object cron {

  type CronInstantSpecification = (Instant) => Boolean
}
