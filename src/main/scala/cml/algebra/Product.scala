package cml.algebra

import cml.Enumerate
import cml.algebra.traits._

import scalaz.{Functor, Monoid}

case class Product[F[_], G[_]] (implicit f_ : Concrete[F], g_ : Concrete[G])
  extends Product.ProductConcrete[F, G] {
  type Type[A] = (F[A], G[A])
}

object Product {
  class ProductAdditive[F, G](implicit f: Additive[F], g: Additive[G])
    extends Additive[(F, G)] {
    override val zero = (f.zero, g.zero)
    override def add(x: (F, G), y: (F, G)): (F, G) = (f.add(x._1, y._1), g.add(x._2, y._2))
    override def neg(x: (F, G)): (F, G) = (f.neg(x._1), g.neg(x._2))
  }

  class ProductRing[F, G](implicit f: Ring[F], g: Ring[G])
    extends ProductAdditive[F, G] with Ring[(F, G)] {
    override val one: (F, G) = (f.one, g.one)
    override def mul(x: (F, G), y: (F, G)): (F, G) = (f.mul(x._1, y._1), g.mul(x._2, y._2))
  }

  class ProductField[F, G](implicit f: Field[F], g: Field[G])
    extends ProductRing[F, G] with Field[(F, G)] {
    override def inv(x: (F, G)): (F, G) = (f.inv(x._1), g.inv((x._2)))
  }

  class ProductFunctor[F[_], G[_]](implicit f: Functor[F], g: Functor[G])
    extends Functor[({type T[A] = (F[A], G[A])})#T] {
    override def map[A, B](v: (F[A], G[A]))(h: (A) => B): (F[B], G[B]) =
      (f.map(v._1)(h), g.map(v._2)(h))
  }

  class ProductAdditive1[F[_], G[_]](implicit f: Additive1[F], g: Additive1[G])
    extends Additive1[({type T[A] = (F[A], G[A])})#T] {
    override def zero[A](implicit a: Additive[A]): (F[A], G[A]) = (f.zero, g.zero)
    override def add[A](x: (F[A], G[A]), y: (F[A], G[A]))(implicit a: Additive[A]): (F[A], G[A]) =
      (f.add(x._1, y._1), g.add(x._2, y._2))
    override def neg[A](x: (F[A], G[A]))(implicit a: Additive[A]): (F[A], G[A]) =
      (f.neg(x._1), g.neg(x._2))
  }

  class ProductLinear[F[_], G[_]](implicit f: Linear[F], g: Linear[G])
    extends ProductAdditive1[F, G]
    with Linear[({type T[A] = (F[A], G[A])})#T] {
    override def mull[A](a: A, v: (F[A], G[A]))(implicit field: Field[A]): (F[A], G[A]) =
      (f.mull(a, v._1), g.mull(a, v._2))
    override def mulr[A](v: (F[A], G[A]), a: A)(implicit field: Field[A]): (F[A], G[A]) =
      (f.mulr(v._1, a), g.mulr(v._2, a))
    override def div[A](v: (F[A], G[A]), a: A)(implicit field: Field[A]): (F[A], G[A]) =
      (f.div(v._1, a), g.div(v._2, a))
  }

  class ProductNormed[F[_], G[_]](implicit f: Normed[F], g: Normed[G])
    extends ProductLinear[F, G]
    with Normed[({type T[A] = (F[A], G[A])})#T] {
    override def sum[A](v: (F[A], G[A]))(implicit a: Additive[A]): A =
      a.add(f.sum(v._1), g.sum(v._2))
    override def taxicab[A](v: (F[A], G[A]))(implicit a: Analytic[A]): A =
      a.add(f.taxicab(v._1), g.taxicab(v._2))
    override def dot[A](u: (F[A], G[A]), v: (F[A], G[A]))(implicit a: Field[A]): A =
      a.add(f.dot(u._1, v._1), g.dot(u._2, v._2))
  }

  class ProductLocallyConcrete[F[_], G[_]](implicit f_ : LocallyConcrete[F], g_ : LocallyConcrete[G])
    extends ProductNormed[F, G]
    with LocallyConcrete[({type T[A] = (F[A], G[A])})#T] {
    val f = f_
    val g = g_

    /**
     * A countable or finite set indexing the basis.
     */
    override type Index = Either[f.Index, g.Index]

    /**
     * The index must be recursively enumerable.
     */
    override def enumerateIndex: Enumerate[Index] = Enumerate.sum(f.enumerateIndex, g.enumerateIndex)

    /**
     * The (finite) dimension of a vector, equal to the dimension of the restriction to v.
     */
    override def dim[A](v: (F[A], G[A]))(implicit field: Field[A]): BigInt =
      f.dim(v._1) + g.dim(v._2)

    /**
     * The (normal) basis for this vector space.
     */
    override def basis[A](i: Index)(implicit field: Field[A]): (F[A], G[A]) =
      i match {
        case Left(j) => (f.basis(j), g.zero)
        case Right(j) => (f.zero, g.basis(j))
      }

    /**
     * Construct a vector from coefficients of the basis vectors.
     */
    override def tabulateLC[A](h: Map[Index, A])(implicit a: Additive[A]): (F[A], G[A]) = {
      val lefts = for ((i, v) <- h; j <- i.left.toSeq) yield (j, v)
      val rights = for ((i, v) <- h; j <- i.right.toSeq) yield (j, v)
      (f.tabulateLC(lefts.toMap), g.tabulateLC(rights.toMap))
    }

    /**
     * Find the coefficient of a basis vector.
     */
    override def indexLC[A](v: (F[A], G[A]))(i: Index)(implicit a: Additive[A]): A =
      i match {
        case Left(j) => f.indexLC(v._1)(j)
        case Right(j) => g.indexLC(v._2)(j)
      }

    /**
     * Maps the vector with a function f. It must hold that f(0) = 0.
     */
    override def mapLC[A, B](x: (F[A], G[A]))(h: (A) => B)(implicit a: Additive[A], b: Additive[B]): (F[B], G[B]) =
      (f.mapLC(x._1)(h), g.mapLC(x._2)(h))

    /**
     * Applies a vector of functions to a vector, pointwise. It must hold that f(0) = 0.
     */
    override def apLC[A, B](x: (F[A], G[A]))(h: (F[A => B], G[A => B]))(implicit a: Additive[A], b: Additive[B]): (F[B], G[B]) =
      (f.apLC(x._1)(h._1), g.apLC(x._2)(h._2))

    /**
     * Returns the concrete subspace containing v.
     */
    override def restrict[A](v: (F[A], G[A]))(implicit field: Field[A]): Concrete[({type T[A] = (F[A], G[A])})#T] =
      Product.concrete(f.restrict(v._1), g.restrict(v._2))

    /**
     * The fundamental property of locally concrete vector spaces is that for any function f on vectors polymorphic in
     * the number type and for each vector v in V, we can factor V as X x Y where X is concrete and f(v) = f(v + y) for
     * all y in Y. This function finds such a subspace X, not necessarily the smallest.
     *
     * It follows that the derivative df(x)/dy = 0 for any y in Y. As such it is enough to consider partial derivatives
     * on X to find the gradient of f.
     *
     * The subspace X does not always depend on the vector v. It only depends on v (and contains restrict(v)) when the
     * function f uses accumulating functions such as sum(), length(), etc. Otherwise the subspace X is constant for
     * all v in V.
     */
    override def restrict[A](h: ((F[A], G[A])) => A)(v: (F[A], G[A]))
        (implicit a: Additive[A]): Concrete[({type T[A] = (F[A], G[A])})#T] = {
      val x = f.restrict((u: F[A]) => h((u, g.zero)))(v._1)
      val y = g.restrict((u: G[A]) => h((f.zero, u)))(v._2)
      concrete[F, G](x, y)
    }
  }

  class ProductConcrete[F[_], G[_]](implicit f_ : Concrete[F], g_ : Concrete[G])
    extends ProductLocallyConcrete[F, G]
    with Concrete[({type T[A] = (F[A], G[A])})#T] {
    override val f = f_
    override val g = g_

    /**
     * The (finite) dimension of this vector space.
     */
    override val dimFin: BigInt = f.dimFin + g.dimFin

    /**
     * Construct a vector from coefficients of the basis vectors.
     */
    override def tabulate[A](h: (Index) => A): (F[A], G[A]) =
      (f.tabulate(i => h(Left(i))), g.tabulate(i => h(Right(i))))

    /**
     * Find the coefficient of a basis vector.
     */
    override def index[A](v: (F[A], G[A]))(i: Index): A =
      i match {
        case Left(j) => f.index(v._1)(j)
        case Right(j) => g.index(v._2)(j)
      }

    override def map[A, B](v: (F[A], G[A]))(h: (A) => B): (F[B], G[B]) =
      (f.map(v._1)(h), g.map(v._2)(h))

    override def point[A](a: => A): (F[A], G[A]) =
      (f.point(a), g.point(a))

    override def ap[A, B](x: => (F[A], G[A]))(h: => (F[(A) => B], G[(A) => B])): (F[B], G[B]) =
      (f.ap(x._1)(h._1), g.ap(x._2)(h._2))

    override def foldMap[A, B](v: (F[A], G[A]))(op: (A) => B)(implicit m: Monoid[B]): B =
      m.append(f.foldMap(v._1)(op), g.foldMap(v._2)(op))

    override def foldRight[A, B](v: (F[A], G[A]), z: => B)(op: (A, => B) => B): B =
      f.foldRight(v._1, g.foldRight(v._2, z)(op))(op)
  }

  implicit def additive[F, G](implicit f: Additive[F], g: Additive[G]): Additive[(F, G)] = new ProductAdditive[F, G]()
  implicit def ring[F, G](implicit f: Ring[F], g: Ring[G]): Ring[(F, G)] = new ProductRing[F, G]()
  implicit def field[F, G](implicit f: Field[F], g: Field[G]): Field[(F, G)] = new ProductField[F, G]()

  implicit def additive1[F[_], G[_]]
    (implicit f: Additive1[F], g: Additive1[G]): Additive1[({type T[A] = (F[A], G[A])})#T] =
    new ProductAdditive1[F, G]()
  implicit def linear[F[_], G[_]]
    (implicit f: Linear[F], g: Linear[G]): Linear[({type T[A] = (F[A], G[A])})#T] =
    new ProductLinear[F, G]()
  implicit def normed[F[_], G[_]]
    (implicit f: Normed[F], g: Normed[G]): Normed[({type T[A] = (F[A], G[A])})#T] =
    new ProductNormed[F, G]()
  implicit def locallyConcrete[F[_], G[_]]
    (implicit f: LocallyConcrete[F], g: LocallyConcrete[G]): LocallyConcrete[({type T[A] = (F[A], G[A])})#T] =
    new ProductLocallyConcrete[F, G]()
  implicit def concrete[F[_], G[_]]
    (implicit f: Concrete[F], g: Concrete[G]): Concrete[({type T[A] = (F[A], G[A])})#T] =
    new ProductConcrete[F, G]()
}
