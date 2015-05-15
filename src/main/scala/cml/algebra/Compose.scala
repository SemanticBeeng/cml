package cml.algebra

import cml.Enumerate
import cml.algebra.traits._

import scalaz.Monoid

case class Compose[F[_], G[_]] (implicit f_ : Concrete[F], g_ : Concrete[G])
  extends Compose.ComposeConcrete[F, G] {
  type Type[A] = F[G[A]]
}

object Compose {
  class ComposeAdditive1[F[_], G[_]](implicit f: Additive1[F], g: Additive1[G])
    extends Additive1[({type T[A] = (F[G[A]])})#T] {
    override def zero[A](implicit field: Additive[A]): F[G[A]] = f.zero[G[A]](g.additive[A])
    override def add[A](x: F[G[A]], y: F[G[A]])(implicit field: Additive[A]): F[G[A]] = f.add(x, y)(g.additive[A])
    override def neg[A](x: F[G[A]])(implicit field: Additive[A]): F[G[A]] = f.neg(x)(g.additive[A])
  }

  class ComposeLinear[F[_], G[_]](implicit f: LocallyConcrete[F], g: Linear[G])
    extends ComposeAdditive1[F, G]
    with Linear[({type T[A] = (F[G[A]])})#T] {
    override def mull[A](a: A, v: F[G[A]])(implicit field: Field[A]): F[G[A]] =
      f.mapLC(v)(g.mull(a, _))(g.additive[A], g.additive[A])
    override def mulr[A](v: F[G[A]], a: A)(implicit field: Field[A]): F[G[A]] =
      f.mapLC(v)(g.mulr(_, a))(g.additive[A], g.additive[A])
    override def div[A](v: F[G[A]], a: A)(implicit field: Field[A]): F[G[A]] =
      f.mapLC(v)(g.div(_, a))(g.additive[A], g.additive[A])
  }

  class ComposeNormed[F[_], G[_]](implicit f: LocallyConcrete[F], g: Normed[G])
    extends ComposeLinear[F, G]
    with Normed[({type T[A] = (F[G[A]])})#T] {
    override def sum[A](v: F[G[A]])(implicit a: Additive[A]): A =
      f.sum(f.mapLC(v)(g.sum(_))(g.additive[A], a))
    override def taxicab[A](v: F[G[A]])(implicit a: Analytic[A]): A =
      f.sum(f.mapLC(v)(g.taxicab(_))(g.additive[A], a))
    override def dot[A](u: F[G[A]], v: F[G[A]])(implicit field: Field[A]): A =
      f.sum(f.apLC(v)(f.mapLC(u)((x: G[A]) => g.dot(x, _: G[A]))(g.additive[A], Function.additive))(g.additive[A], field))
  }

  class ComposeLocallyConcrete[F[_], G[_]](implicit f_ : LocallyConcrete[F], g_ : LocallyConcrete[G])
    extends ComposeNormed[F, G]
    with LocallyConcrete[({type T[A] = (F[G[A]])})#T] {
    val f = f_
    val g = g_

    /**
     * A countable or finite set indexing the basis.
     */
    override type Index = (f.Index, g.Index)

    /**
     * The index must be recursively enumerable.
     */
    override def enumerateIndex: Enumerate[Index] =
      Enumerate.product(f.enumerateIndex, g.enumerateIndex)
    
    /**
     * The (normal) basis for this vector space.
     */
    override def basis[A](i: Index)(implicit field: Field[A]): F[G[A]] = {
      val fb = f.basis(i._1)
      val gb = g.basis(i._2)
      f.mapLC(fb)(g.mull(_, gb))(field, g.additive)
    }

    /**
     * Construct a vector from coefficients of the basis vectors.
     */
    override def tabulateLC[A](h: Map[Index, A])(implicit a: Additive[A]): F[G[A]] =
      f.tabulateLC(h.groupBy(_._1._1).mapValues(x => g.tabulateLC(x.map{ case (k, v) => (k._2, v) })))(g.additive)

    /**
     * Find the coefficient of a basis vector.
     */
    override def indexLC[A](v: F[G[A]])(i: Index)(implicit a: Additive[A]): A =
      g.indexLC(f.indexLC(v)(i._1)(g.additive))(i._2)

    /**
     * Maps the vector with a function f. It must hold that f(0) = 0.
     */
    override def mapLC[A, B](x: F[G[A]])(h: (A) => B)(implicit a: Additive[A], b: Additive[B]): F[G[B]] =
      f.mapLC(x)(g.mapLC(_)(h))(g.additive, g.additive)

    /**
     * Applies a vector of functions to a vector, pointwise. It must hold that f(0) = 0.
     */
    override def apLC[A, B](x: F[G[A]])(h: F[G[(A) => B]])(implicit a: Additive[A], b: Additive[B]): F[G[B]] =
      f.apLC(x)(
        f.mapLC(h)(
          (gab: G[A => B]) => g.apLC(_: G[A])(gab)
        )(g.additive[(A) => B](Function.additive), Function.additive[G[A], G[B]](g.additive)))(g.additive[A], g.additive[B])

    /**
     * Returns the concrete subspace containing v.
     */
    override def restrict[A](v: F[G[A]])(implicit field: Field[A]): Concrete[({type T[A] = (F[G[A]])})#T] = {
      val fv: F[A] = f.mapLC(v)(g.sum(_))(g.additive, field)
      val gv: G[A] = f.sum(v)(g.additive)
      Compose.concrete(f.restrict(fv), g.restrict(gv))
    }
  }

  class ComposeConcrete[F[_], G[_]](implicit f_ : Concrete[F], g_ : Concrete[G])
    extends ComposeLocallyConcrete[F, G]
    with Concrete[({type T[A] = (F[G[A]])})#T] {
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

    /**
     * Find the coefficient of a basis vector.
     */
    def index[A](v: F[G[A]])(i: Index): A =
      g.index(f.index(v)(i._1))(i._2)

    override def map[A, B](v: F[G[A]])(h: (A) => B): F[G[B]] =
      f.map(v)(g.map(_)(h))

    override def point[A](a: => A): F[G[A]] =
      f.point(g.point(a))

    override def ap[A, B](x: => F[G[A]])(h: => F[G[(A) => B]]): F[G[B]] =
      f.ap(x)(f.map(h)(gab => g.ap(_)(gab)))

    override def foldMap[A, B](v: F[G[A]])(h: (A) => B)(implicit F: Monoid[B]): B =
      f.foldMap(v)(g.foldMap(_)(h))

    override def foldRight[A, B](v: F[G[A]], z: => B)(h: (A, => B) => B): B =
      f.foldRight(v, z)(g.foldRight(_, _)(h))
  }

  implicit def additive1[F[_], G[_]]
    (implicit f: Concrete[F], g: Additive1[G]): Additive1[Compose[F, G]#Type] =
    new ComposeAdditive1[F, G]()
  implicit def linear[F[_], G[_]]
    (implicit f: Concrete[F], g: Linear[G]): Linear[Compose[F, G]#Type] =
    new ComposeLinear[F, G]()
  implicit def normed[F[_], G[_]]
    (implicit f: Concrete[F], g: Normed[G]): Normed[Compose[F, G]#Type] =
    new ComposeNormed[F, G]()
  implicit def locallyConcrete[F[_], G[_]]
    (implicit f: LocallyConcrete[F], g: LocallyConcrete[G]): LocallyConcrete[Compose[F, G]#Type] =
    new ComposeLocallyConcrete[F, G]()
  implicit def concrete[F[_], G[_]]
    (implicit f: Concrete[F], g: Concrete[G]): Concrete[Compose[F, G]#Type] =
    new ComposeConcrete[F, G]()
}
