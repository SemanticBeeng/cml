package cml.algebra.traits

trait Normed[V[_]] extends Linear[V] {
  def sum[F](v: V[F])(implicit f: Additive[F]): F
  def taxicab[F](v: V[F])(implicit f: Analytic[F]): F
  def length[F](v: V[F])(implicit f: Analytic[F]): F = f.sqrt(quadrance(v))
  def dist[F](u: V[F], v: V[F])(implicit f: Analytic[F]): F = length(sub(u, v))
  def dot[F](u: V[F], v: V[F])(implicit f: Field[F]): F
  def quadrance[F](v: V[F])(implicit f: Field[F]): F = dot(v, v)
}
