import cml.algebra.traits._
import cml.algebra.Real._
import cml.algebra
import shapeless.Nat

object AlgebraTest extends App {
  def fun[F](x: F)(implicit f: Additive[F]): F = f.add(x, x)
  def fun2[V[_], F](x: V[F])(implicit s: Additive1[V], f: Field[F]): V[F] = s.add(x, x)
  println(fun[Double](2))

  implicit val vec2 = algebra.Vector(Nat(2))
  implicit val vec3 = algebra.Vector(Nat(3))
  val vec2b = algebra.Vector(Nat(2))

  println(vec2.dim)
  println(vec3.dim)

  val u: vec3.Type[Double] = vec3.point(2.0)
  val v = vec3.mull(3.0, u)
  println(u)
  println(v)

  val w = vec2.point(1.0)
  val r = vec2.add(w, vec2b.point(3.0))
  println(w)

  println(fun2[vec2.Type, Double](w))
}
