package cml.algebra.traits

import cml.syntax.AdditiveSyntax

trait Additive[T] {
  val zero: T
  def add(x: T, y: T): T
  def sub(x: T, y: T): T = add(x, neg(y))
  def neg(x: T): T

  val additiveSyntax = new AdditiveSyntax[T] { def F = Additive.this }
}
