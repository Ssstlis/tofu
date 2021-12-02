package tofu.memo

import cats.effect.{Concurrent, ExitCase}
import cats.effect.concurrent.{Deferred, Ref}
import simulacrum.typeclass
import tofu.syntax.monadic._
import cats.syntax.option._
import cats.effect.syntax.concurrent._
import cats.effect.syntax.bracket._

import scala.annotation.nowarn
import scala.annotation.implicitNotFound

/** WARNING breaks referential transparency, use with great care */
@implicitNotFound("Could not find an instance of Memoize for ${F}")
@typeclass @nowarn("cat=unused-imports")
trait Memoize[F[_]] extends Serializable {
  def memoize[A](fa: F[A]): F[F[A]]

  /** should be redefined if F is at least ApplicativeError */
  def memoizeOnSuccess[A](fa: F[A]): F[F[A]]
}

object Memoize {
  def concurrentMemoize[F[_]](implicit F: Concurrent[F]): Memoize[F] =
    new Memoize[F] {
      def memoize[A](fa: F[A]): F[F[A]] = Concurrent.memoize(fa)

      // copy of Concurrent.memoize accepting success only
      def memoizeOnSuccess[A](f: F[A]): F[F[A]] = {
        {
          sealed trait State
          case class Subs(n: Int) extends State
          case object Done        extends State

          case class Fetch(state: State, v: Deferred[F, A], stop: Deferred[F, F[Unit]])

          Ref[F].of(Option.empty[Fetch]).map { state =>
            (Deferred[F, A] product Deferred[F, F[Unit]]).flatMap { case (v, stop) =>
              def endState(ec: ExitCase[Throwable]) =
                state.modify {
                  case None                          => throw new AssertionError("unreachable")
                  case s @ Some(Fetch(Done, _, _))   => s -> F.unit
                  case Some(Fetch(Subs(n), v, stop)) =>
                    if (ec == ExitCase.Canceled && n == 1) None -> stop.get.flatten
                    else if (ec == ExitCase.Canceled) Fetch(Subs(n - 1), v, stop).some -> F.unit
                    else Fetch(Done, v, stop).some                                     -> F.unit
                }.flatten

              def fetch =
                f.flatMap(v.complete)
                  .start
                  .flatMap(fiber => stop.complete(fiber.cancel))

              state.modify {
                case s @ Some(Fetch(Done, v, _))   =>
                  s -> v.get
                case Some(Fetch(Subs(n), v, stop)) =>
                  Fetch(Subs(n + 1), v, stop).some -> v.get.guaranteeCase(endState)
                case None                          =>
                  Fetch(Subs(1), v, stop).some -> fetch.bracketCase(_ => v.get) { case (_, ec) => endState(ec) }
              }.flatten
            }
          }
        }
      }
    }

  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /**
   * Summon an instance of [[Memoize]] for `F`.
   */
  @inline def apply[F[_]](implicit instance: Memoize[F]): Memoize[F] = instance

  object ops {
    implicit def toAllMemoizeOps[F[_], A](target: F[A])(implicit tc: Memoize[F]): AllOps[F, A] {
      type TypeClassType = Memoize[F]
    } = new AllOps[F, A] {
      type TypeClassType = Memoize[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  trait Ops[F[_], A] extends Serializable {
    type TypeClassType <: Memoize[F]
    def self: F[A]
    val typeClassInstance: TypeClassType
    def memoize: F[F[A]] = typeClassInstance.memoize[A](self)
    def memoizeOnSuccess: F[F[A]] = typeClassInstance.memoizeOnSuccess[A](self)
  }
  trait AllOps[F[_], A] extends Ops[F, A]
  trait ToMemoizeOps extends Serializable {
    implicit def toMemoizeOps[F[_], A](target: F[A])(implicit tc: Memoize[F]): Ops[F, A] {
      type TypeClassType = Memoize[F]
    } = new Ops[F, A] {
      type TypeClassType = Memoize[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  object nonInheritedOps extends ToMemoizeOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}
