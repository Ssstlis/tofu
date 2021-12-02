package tofu.common

import java.io.{BufferedReader, PrintStream}

import simulacrum.typeclass
import tofu.higherKind
import tofu.higherKind.RepresentableK

import scala.annotation.nowarn
import scala.{Console => ScalaConsole}
import tofu.Delay
import scala.annotation.implicitNotFound

@implicitNotFound("Could not find an instance of Console for ${F}")
@typeclass @nowarn("cat=unused-imports")
trait Console[F[_]] extends Serializable {
  def readStrLn: F[String]

  def putStr(s: String): F[Unit]
  def putStrLn(s: String): F[Unit]

  def putErr(err: String): F[Unit]
  def putErrLn(err: String): F[Unit]
}

object Console {
  @deprecated("use tofu.syntax.console", since = "0.7.2")
  def readStrLn[F[_]](implicit F: Console[F]): F[String] = F.readStrLn

  @deprecated("use tofu.syntax.console", since = "0.7.2")
  def putStr[F[_]](s: String)(implicit F: Console[F]): F[Unit] = F.putStr(s)

  @deprecated("use tofu.syntax.console", since = "0.7.2")
  def putStrLn[F[_]](s: String)(implicit F: Console[F]): F[Unit] = F.putStrLn(s)

  @deprecated("use tofu.syntax.console", since = "0.7.2")
  def putErr[F[_]](e: String)(implicit F: Console[F]): F[Unit] = F.putErr(e)

  @deprecated("use tofu.syntax.console", since = "0.7.2")
  def putErrLn[F[_]](e: String)(implicit F: Console[F]): F[Unit] = F.putErrLn(e)

  implicit val representableKInstance: RepresentableK[Console] = higherKind.derived.genRepresentableK[Console]

  implicit def delayInstance[F[_]: Delay]: Console[F] = instance(ScalaConsole.in, ScalaConsole.out, ScalaConsole.err)

  def instance[F[_]](in: BufferedReader, out: PrintStream, err: PrintStream)(implicit FD: Delay[F]): Console[F] =
    new Console[F] {
      def readStrLn: F[String] = FD.delay(in.readLine())

      def putStr(s: String): F[Unit]   = FD.delay(out.print(s))
      def putStrLn(s: String): F[Unit] = FD.delay(out.println(s))

      def putErr(e: String): F[Unit]   = FD.delay(err.print(e))
      def putErrLn(e: String): F[Unit] = FD.delay(err.println(e))
    }

  /* ======================================================================== */
  /* THE FOLLOWING CODE IS MANAGED BY SIMULACRUM; PLEASE DO NOT EDIT!!!!      */
  /* ======================================================================== */

  /**
   * Summon an instance of [[Console]] for `F`.
   */
  @inline def apply[F[_]](implicit instance: Console[F]): Console[F] = instance

  object ops {
    implicit def toAllConsoleOps[F[_], A](target: F[A])(implicit tc: Console[F]): AllOps[F, A] {
      type TypeClassType = Console[F]
    } = new AllOps[F, A] {
      type TypeClassType = Console[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  trait Ops[F[_], A] extends Serializable {
    type TypeClassType <: Console[F]
    def self: F[A]
    val typeClassInstance: TypeClassType
  }
  trait AllOps[F[_], A] extends Ops[F, A]
  trait ToConsoleOps extends Serializable {
    implicit def toConsoleOps[F[_], A](target: F[A])(implicit tc: Console[F]): Ops[F, A] {
      type TypeClassType = Console[F]
    } = new Ops[F, A] {
      type TypeClassType = Console[F]
      val self: F[A] = target
      val typeClassInstance: TypeClassType = tc
    }
  }
  object nonInheritedOps extends ToConsoleOps

  /* ======================================================================== */
  /* END OF SIMULACRUM-MANAGED CODE                                           */
  /* ======================================================================== */

}
