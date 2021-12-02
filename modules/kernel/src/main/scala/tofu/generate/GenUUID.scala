package tofu.generate

import java.util.UUID

import cats.Functor
import cats.syntax.functor._
import simulacrum.typeclass
import tofu.higherKind
import tofu.higherKind.RepresentableK

import scala.annotation.nowarn
import tofu.Delay
import scala.annotation.implicitNotFound

@implicitNotFound("Could not find an instance of GenUUID for ${F}")
@typeclass @nowarn("cat=unused-imports")
trait GenUUID[F[_]] extends Serializable {
  def randomUUID: F[UUID]
}

object GenUUID {
  def random[F[_]](implicit gen: GenUUID[F]): F[UUID] = gen.randomUUID
  def randomString[F[_]: Functor: GenUUID]: F[String] = random[F].map(_.toString)

  implicit def syncGenUUID[F[_]](implicit F: Delay[F]): GenUUID[F] = new GenUUID[F] {
    val randomUUID: F[UUID] = F.delay(UUID.randomUUID())
  }

  implicit val genUUIDRepresentableK: RepresentableK[GenUUID] = higherKind.derived.genRepresentableK[GenUUID]

  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /**
   * Summon an instance of [[GenUUID]] for `F`.
   */
  @inline def apply[F[_]](implicit instance: GenUUID[F]): GenUUID[F] = instance

  object ops {
    implicit def toAllGenUUIDOps[F[_], A](target: F[A])(implicit tc: GenUUID[F]): AllOps[F, A] {
      type TypeClassType = GenUUID[F]
    } = new AllOps[F, A] {
      type TypeClassType = GenUUID[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  trait Ops[F[_], A] extends Serializable {
    type TypeClassType <: GenUUID[F]
    def self: F[A]
    val typeClassInstance: TypeClassType
  }
  trait AllOps[F[_], A] extends Ops[F, A]
  trait ToGenUUIDOps extends Serializable {
    implicit def toGenUUIDOps[F[_], A](target: F[A])(implicit tc: GenUUID[F]): Ops[F, A] {
      type TypeClassType = GenUUID[F]
    } = new Ops[F, A] {
      type TypeClassType = GenUUID[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  object nonInheritedOps extends ToGenUUIDOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}
