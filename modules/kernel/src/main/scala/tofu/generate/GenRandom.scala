package tofu.generate

import simulacrum.typeclass
import tofu.higherKind
import tofu.higherKind.RepresentableK
import tofu.syntax.monadic._

import scala.annotation.nowarn
import scala.util.Random
import tofu.Delay
import cats.Functor
import scala.annotation.implicitNotFound

@implicitNotFound("Could not find an instance of GenRandom for ${F}")
@typeclass @nowarn("cat=unused-imports")
trait GenRandom[F[_]] extends Serializable {

  /** generate random 8-bit number */
  def nextLong: F[Long]

  /** Returns a pseudorandom, uniformly distributed int value between 0 (inclusive) and the specified value (exclusive),
    * drawn from this random number generator's sequence.
    */
  def nextInt(n: Int): F[Int]
}

object GenRandom {
  def nextLong[F[_]](implicit g: GenRandom[F]): F[Long]       = g.nextLong
  def nextInt[F[_]](n: Int)(implicit g: GenRandom[F]): F[Int] = g.nextInt(n)

  def instance[I[_]: Delay: Functor, F[_]: Delay](
      seed: Option[Long] = None,
      secure: Boolean = false
  ): I[GenRandom[F]] = {
    def createStd()    = seed.fold(new java.util.Random)(new java.util.Random(_))
    def createSecure() = {
      val rnd = new java.security.SecureRandom()
      seed.foreach(rnd.setSeed)
      rnd
    }

    def random(): java.util.Random = if (secure) createSecure() else createStd()
    for (rnd <- Delay[I].delay(new Random(random()))) yield new ScalaUtil[F](rnd)
  }

  private class ScalaUtil[F[_]](rnd: Random)(implicit F: Delay[F]) extends GenRandom[F] {
    def nextLong: F[Long]         = F.delay(rnd.nextLong())
    def nextInt(max: Int): F[Int] = F.delay(rnd.nextInt(max))
  }

  implicit val genRandomRepresentableK: RepresentableK[GenRandom] = higherKind.derived.genRepresentableK[GenRandom]

  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /**
   * Summon an instance of [[GenRandom]] for `F`.
   */
  @inline def apply[F[_]](implicit instance: GenRandom[F]): GenRandom[F] = instance

  object ops {
    implicit def toAllGenRandomOps[F[_], A](target: F[A])(implicit tc: GenRandom[F]): AllOps[F, A] {
      type TypeClassType = GenRandom[F]
    } = new AllOps[F, A] {
      type TypeClassType = GenRandom[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  trait Ops[F[_], A] extends Serializable {
    type TypeClassType <: GenRandom[F]
    def self: F[A]
    val typeClassInstance: TypeClassType
  }
  trait AllOps[F[_], A] extends Ops[F, A]
  trait ToGenRandomOps extends Serializable {
    implicit def toGenRandomOps[F[_], A](target: F[A])(implicit tc: GenRandom[F]): Ops[F, A] {
      type TypeClassType = GenRandom[F]
    } = new Ops[F, A] {
      type TypeClassType = GenRandom[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  object nonInheritedOps extends ToGenRandomOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}
