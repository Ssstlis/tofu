package tofu.control

import cats.data.{Chain, WriterT}

object SelectiveSummon {
  Selective[WriterT[Option, Chain[Int], *]]
}
