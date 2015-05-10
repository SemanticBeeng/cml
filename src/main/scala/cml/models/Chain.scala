package cml.models

import cml.algebra.traits._
import cml.algebra

case class Chain2[-In[_], Mid[_], +Out[_]] (
  m1: Model[In, Mid],
  m2: Model[Mid, Out]
) extends Model[In, Out] {
  override type Type[A] = (m1.Type[A], m2.Type[A])

  override implicit val traverse = algebra.Product.traverse[m1.Type, m2.Type](m1.traverse, m2.traverse)
  override implicit val linear = algebra.Product.linear[m1.Type, m2.Type](m1.linear, m2.linear)

  override def apply[F](input: In[F])(model: Type[F])(implicit f: Analytic[F]): Out[F] =
    m2(m1(input)(model._1))(model._2)
}

case class Chain3[-In[_], Mid1[_], Mid2[_], +Out[_]] (
  m1: Model[In, Mid1],
  m2: Model[Mid1, Mid2],
  m3: Model[Mid2, Out]
) extends Model[In, Out] {
  val chain = Chain2(m2, m3)

  override type Type[A] = (m1.Type[A], chain.Type[A])

  override implicit val traverse = algebra.Product.traverse[m1.Type, chain.Type](m1.traverse, chain.traverse)
  override implicit val linear = algebra.Product.linear[m1.Type, chain.Type](m1.linear, chain.linear)

  override def apply[F](input: In[F])(model: Type[F])(implicit f: Analytic[F]): Out[F] =
    chain(m1(input)(model._1))(model._2)
}

case class Chain4[-In[_], Mid1[_], Mid2[_], Mid3[_], +Out[_]] (
  m1: Model[In, Mid1],
  m2: Model[Mid1, Mid2],
  m3: Model[Mid2, Mid3],
  m4: Model[Mid3, Out]
) extends Model[In, Out] {
  val chain = Chain3(m2, m3, m4)

  override type Type[A] = (m1.Type[A], chain.Type[A])

  override implicit val traverse = algebra.Product.traverse[m1.Type, chain.Type](m1.traverse, chain.traverse)
  override implicit val linear = algebra.Product.linear[m1.Type, chain.Type](m1.linear, chain.linear)

  override def apply[F](input: In[F])(model: Type[F])(implicit f: Analytic[F]): Out[F] =
    chain(m1(input)(model._1))(model._2)
}

case class Chain5[-In[_], Mid1[_], Mid2[_], Mid3[_], Mid4[_], +Out[_]] (
  m1: Model[In, Mid1],
  m2: Model[Mid1, Mid2],
  m3: Model[Mid2, Mid3],
  m4: Model[Mid3, Mid4],
  m5: Model[Mid4, Out]
) extends Model[In, Out] {
  val chain = Chain4(m2, m3, m4, m5)

  override type Type[A] = (m1.Type[A], chain.Type[A])

  override implicit val traverse = algebra.Product.traverse[m1.Type, chain.Type](m1.traverse, chain.traverse)
  override implicit val linear = algebra.Product.linear[m1.Type, chain.Type](m1.linear, chain.linear)

  override def apply[F](input: In[F])(model: Type[F])(implicit f: Analytic[F]): Out[F] =
    chain(m1(input)(model._1))(model._2)
}

case class Chain6[-In[_], Mid1[_], Mid2[_], Mid3[_], Mid4[_], Mid5[_], +Out[_]] (
  m1: Model[In, Mid1],
  m2: Model[Mid1, Mid2],
  m3: Model[Mid2, Mid3],
  m4: Model[Mid3, Mid4],
  m5: Model[Mid4, Mid5],
  m6: Model[Mid5, Out]
) extends Model[In, Out] {
  val chain = Chain5(m2, m3, m4, m5, m6)

  override type Type[A] = (m1.Type[A], chain.Type[A])

  override implicit val traverse = algebra.Product.traverse[m1.Type, chain.Type](m1.traverse, chain.traverse)
  override implicit val linear = algebra.Product.linear[m1.Type, chain.Type](m1.linear, chain.linear)

  override def apply[F](input: In[F])(model: Type[F])(implicit f: Analytic[F]): Out[F] =
    chain(m1(input)(model._1))(model._2)
}

case class Chain7[-In[_], Mid1[_], Mid2[_], Mid3[_], Mid4[_], Mid5[_], Mid6[_], +Out[_]] (
  m1: Model[In, Mid1],
  m2: Model[Mid1, Mid2],
  m3: Model[Mid2, Mid3],
  m4: Model[Mid3, Mid4],
  m5: Model[Mid4, Mid5],
  m6: Model[Mid5, Mid6],
  m7: Model[Mid6, Out]
) extends Model[In, Out] {
  val chain = Chain6(m2, m3, m4, m5, m6, m7)

  override type Type[A] = (m1.Type[A], chain.Type[A])

  override implicit val traverse = algebra.Product.traverse[m1.Type, chain.Type](m1.traverse, chain.traverse)
  override implicit val linear = algebra.Product.linear[m1.Type, chain.Type](m1.linear, chain.linear)

  override def apply[F](input: In[F])(model: Type[F])(implicit f: Analytic[F]): Out[F] =
    chain(m1(input)(model._1))(model._2)
}