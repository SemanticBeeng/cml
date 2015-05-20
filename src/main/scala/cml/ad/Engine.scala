package cml.ad

import cml.algebra.traits._

/**
 * Automatic differentiation engine.
 */
trait Engine {
  type Aug[_]

  /**
   * Aug[F] is a field given that F is one.
   */
  implicit def field[F](implicit f: Field[F]): Field[Aug[F]]

  /**
   * Aug[F] is an analytic field given that F is one.
   */
  implicit def analytic[F](implicit f: Analytic[F]): Analytic[Aug[F]]

  /**
   * Injects a constant value into the augmented field.
   */
  def constant[F](x: F)(implicit field: Field[F]): Aug[F]

  /**
   * Differentiates a function.
   */
  def diff[F](f: (Aug[F]) => Aug[F])(implicit field: Field[F]): (F) => F

  /**
   * Computes a function value and its derivative.
   */
  def diffWithValue[F](f: (Aug[F]) => Aug[F])(implicit field: Field[F]): (F) => (F, F)

  /**
   * Computes the gradient of a function taking a vector as the argument.
   */
  def grad[F, V[_]](f: (V[Aug[F]]) => Aug[F])(implicit field: Field[F], space: Concrete[V]): (V[F]) => V[F]

  /**
   * Computes the value and gradient of a function taking a vector as the argument.
   */
  def gradWithValue[F, V[_]](f: (V[Aug[F]]) => Aug[F])(implicit field: Field[F], space: Concrete[V]): (V[F]) => (F, V[F])

  /**
   * Computes the gradient of a function taking a vector as the argument.
   */
  def gradLC[F, V[_]](f: (V[Aug[F]]) => Aug[F])(implicit an: Analytic[F], space: LocallyConcrete[V]): (V[F]) => V[F] =
    x => grad[F, V](f)(an, space.restrict(f)(space.mapLC(x)(constant(_))))(x)

  /**
   * Computes the value and gradient of a function taking a vector as the argument.
   */
  def gradWithValueLC[F, V[_]](f: V[Aug[F]] => Aug[F])(implicit an: Analytic[F], space: LocallyConcrete[V]): (V[F]) => (F, V[F]) =
    x => gradWithValue[F, V](f)(an, space.restrict(f)(space.mapLC(x)(constant(_))))(x)
}