package cml.models

import cml.algebra.{AnalyticMap, Analytic}
import cml.{Model, algebra}
import cml.algebra.traits._
import shapeless.Nat

case class Pointwise[V[_]] (
  f: AnalyticMap
) (
  implicit c: Concrete[V]
) extends Model[V, V] {
  val vec = algebra.Vec(Nat(0))
  override type Type[A] = vec.Type[A]

  override implicit val space = vec

  def apply[F](inst: Type[F])(input: V[F])(implicit field: Analytic[F]): V[F] =
    c.map(input)(f(_))
}
