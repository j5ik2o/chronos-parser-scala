package com.github.j5ik2o.cron.ast

trait ExprVisitor[T] {
  def visit(e: Expr): T
}
