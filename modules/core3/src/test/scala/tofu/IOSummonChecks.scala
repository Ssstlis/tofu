package tofu

import cats.data.ReaderT
import cats.effect.{Concurrent, IO}

class IOSummonChecks {
  implicitly[Start[IO[*]]]
  implicitly[Start[ReaderT[IO, Unit, *]]]

  def summonInstancesForConcurrent[F[_]: Concurrent] = {
    implicitly[Start[F]]
    implicitly[Start[ReaderT[F, Unit, *]]]
  }
}
