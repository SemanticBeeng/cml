package cml.algebra

import cml.Enumerate
import cml.algebra.traits._
import shapeless.ops.nat.{Prod, ToInt}

import scalaz.Monoid

object Compose {
  class ComposeAdditive1[F[_], G[_]](implicit f: Additive1[F], g: Additive1[G])
    extends Additive1[({type T[A] = F[G[A]]})#T] {
    override def zero[A](implicit field: Additive[A]): F[G[A]] = f.zero[G[A]](g.additive[A])
    override def add[A](x: F[G[A]], y: F[G[A]])(implicit field: Additive[A]): F[G[A]] = f.add(x, y)(g.additive[A])
    override def neg[A](x: F[G[A]])(implicit field: Additive[A]): F[G[A]] = f.neg(x)(g.additive[A])
  }

  class ComposeLinear[F[_], G[_]](implicit f: LocallyConcrete[F], g: Linear[G])
    extends ComposeAdditive1[F, G]
    with Linear[({type T[A] = F[G[A]]})#T] {
    override def mull[A](a: A, v: F[G[A]])(implicit field: Field[A]): F[G[A]] = f.map(v)(g.mull(a, _))
    override def mulr[A](v: F[G[A]], a: A)(implicit field: Field[A]): F[G[A]] = f.map(v)(g.mulr(_, a))
    override def div[A](v: F[G[A]], a: A)(implicit field: Field[A]): F[G[A]] = f.map(v)(g.div(_, a))
  }

  class ComposeNormed[F[_], G[_]](implicit f: LocallyConcrete[F], g: Normed[G])
    extends ComposeLinear[F, G]
    with Normed[({type T[A] = F[G[A]]})#T] {
    override def sum[A](v: F[G[A]])(implicit a: Additive[A]): A = f.sum(f.map(v)(g.sum(_)))
    override def taxicab[A](v: F[G[A]])(implicit field: Analytic[A]): A = f.sum(f.map(v)(g.taxicab(_)))
    override def dot[A](u: F[G[A]], v: F[G[A]])(implicit field: Field[A]): A = f.sum(f.apply2(u, v)(g.dot(_, _)))
  }

  class ComposeLocallyConcrete[F[_], G[_]](implicit f_ : LocallyConcrete[F], g_ : LocallyConcrete[G])
    extends ComposeNormed[F, G]
    with LocallyConcrete[({type T[A] = F[G[A]]})#T] {
    val f = f_
    val g = g_

    /**
     * A countable or finite set indexing the basis.
     */
    override type Index = (f.Index, g.Index)

    /**
     * The index must be recursively enumerable.
     */
    override def enumerateIndex: Enumerate[Index] = Enumerate.product(f.enumerateIndex, g.enumerateIndex)

    /**
     * Construct a vector from coefficients of the basis vectors.
     */
    override def tabulate[A](h: Map[Index, A])(implicit a: Additive[A]): F[G[A]] =
      f.tabulate(h.groupBy(_._1._1).mapValues(x => g.tabulate(x.map{ case (k, v) => (k._2, v) })))(g.additive)

    /**
     * Find the coefficient of a basis vector.
     */
    override def index[A](v: F[G[A]])(i: Index): A =
      g.index(f.index(v)(i._1))(i._2)

    /**
     * The (normal) basis for this vector space.
     */
    override def basis[A](i: Index)(implicit field: Field[A]): F[G[A]] = {
      val fb = f.basis(i._1)
      val gb = g.basis(i._2)
      f.map(fb)(g.mull(_, gb))
    }

    /**
     * Returns the concrete subspace containing v.
     */
    override def restrict[A](v: F[G[A]])(implicit field: Field[A]): Concrete[({type T[A] = (F[G[A]])})#T] = {
      val fv: F[A] = f.map(v)(g.sum(_))
      val gv: G[A] = f.sum(v)(g.additive)
      Compose.concrete(f.restrict(fv), g.restrict(gv))
    }

    override def map[A, B](v: F[G[A]])(h: (A) => B): F[G[B]] =
      f.map(v)(g.map(_)(h))

    override def ap[A, B](x: => F[G[A]])(h: => F[G[(A) => B]]): F[G[B]] =
      f.ap(x)(f.map(h)(gab => g.ap(_)(gab)))
  }

  class ComposeConcrete[F[_], G[_]](implicit f_ : Concrete[F], g_ : Concrete[G])
    extends ComposeLocallyConcrete[F, G]
    with Concrete[({type T[A] = F[G[A]]})#T] {
    override val f = f_
    override val g = g_

    /**
     * The (finite) dimension of this vector space.
     */
    override val dimFin: BigInt = f.dimFin * g.dimFin

    /**
     * Construct a vector from coefficients of the basis vectors.
     */
    override def tabulate[A](h: (Index) => A): F[G[A]] =
      f.tabulate(i => g.tabulate(j => h((i, j))))

    override def point[A](a: => A): F[G[A]] =
      f.point(g.point(a))

    override def pointwise[A](h: AnalyticMap)(v: F[G[A]])(implicit field: Analytic[A]): F[G[A]] =
      f.map(v)(g.pointwise(h)(_))

    override def foldMap[A, B](v: F[G[A]])(h: (A) => B)(implicit F: Monoid[B]): B =
      f.foldMap(v)(g.foldMap(_)(h))

    override def foldRight[A, B](v: F[G[A]], z: => B)(h: (A, => B) => B): B =
      f.foldRight(v, z)(g.foldRight(_, _)(h))
  }

  implicit def additive1[F[_], G[_]]
    (implicit f: Concrete[F], g: Additive1[G]): Additive1[({type T[A] = F[G[A]]})#T] =
    new ComposeAdditive1[F, G]()
  implicit def linear[F[_], G[_]]
    (implicit f: Concrete[F], g: Linear[G]): Linear[({type T[A] = F[G[A]]})#T] =
    new ComposeLinear[F, G]()
  implicit def normed[F[_], G[_]]
    (implicit f: Concrete[F], g: Normed[G]): Normed[({type T[A] = F[G[A]]})#T] =
    new ComposeNormed[F, G]()
  implicit def locallyConcrete[F[_], G[_]]
    (implicit f: LocallyConcrete[F], g: LocallyConcrete[G]): LocallyConcrete[({type T[A] = F[G[A]]})#T] =
    new ComposeLocallyConcrete[F, G]()
  implicit def concrete[F[_], G[_]]
    (implicit f: Concrete[F], g: Concrete[G]): Concrete[({type T[A] = F[G[A]]})#T] =
    new ComposeConcrete[F, G]()
}
