package cml.models

import cml.Model
import cml.algebra._

import scalaz.Bifunctor

final case class BifunctorMap[F[_, _], A1[_], A2[_], B1[_], B2[_]] (
  left: Model[A1, A2],
  right: Model[B1, B2]
) (implicit
  f: T forSome {type T <: Bifunctor[F] with Serializable}
) extends Model[({type T[A] = F[A1[A], B1[A]]})#T, ({type T[A] = F[A2[A], B2[A]]})#T] {
  override type Params[A] = (left.Params[A], right.Params[A])

  override implicit val params = Representable.product(left.params, right.params)

  override def apply[A](inst: Params[A])(input: F[A1[A], B1[A]])(implicit a: Analytic[A]): F[A2[A], B2[A]] =
    f.bimap(input)(left(inst._1)(_), right(inst._2)(_))
}
