package com.github.j5ik2o.cron.ast

trait Expr {

  def accept[T](visitor: ExprVisitor[T]): T = {
    visitor.visit(this)
  }
}

case class NoOp() extends Expr

case class ValueExpr(digit: Int) extends Expr

case class LastValueExpr() extends Expr

case class AnyValueExpr() extends Expr

case class PerExpr(digit: Expr, option: Expr) extends Expr

case class RangeExpr(from: Expr, to: Expr, perOtion: Expr) extends Expr

case class ListExpr(exprs: List[Expr]) extends Expr

case class CronExpr(mins: Expr, hours: Expr, days: Expr, months: Expr, dayOfWeeks: Expr) extends Expr
