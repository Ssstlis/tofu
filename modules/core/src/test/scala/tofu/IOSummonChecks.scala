package tofu

import cats.data.ReaderT
import cats.effect.{Concurrent, ContextShift, IO}

import scala.annotation.nowarn

@nowarn("cat=unused-params")
class IOSummonChecks {
  def summonForIO(implicit cs: ContextShift[IO]) = {
    implicitly[Start[IO[*]]]
    implicitly[Start[ReaderT[IO, Unit, *]]]
  }

  def summonInstancesForConcurrent[F[_]: Concurrent] = {
    implicitly[Start[F]]
    implicitly[Start[ReaderT[F, Unit, *]]]
  }
}
