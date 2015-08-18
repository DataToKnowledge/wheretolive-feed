package it.dtk.feed

import it.dtk.feed.Model._

import scala.concurrent.duration.{FiniteDuration, _}

object FeedSchedulerUtil {

  val minTime: FiniteDuration = 4 minutes

  def gotException(f: FeedScheduler): FeedScheduler =
    f.copy(time = f.time * 2)

  def when(f: FeedScheduler, numUrls: Int): FeedScheduler = numUrls match {
    case -1 => f
    case x: Int if x >= 5 =>
      val nextTime = if (f.time < minTime)
        minTime
      else f.time - minTime

      f.copy(time = nextTime)

    case x: Int if x < 5 =>
      val nextTime = f.time + f.delta
      f.copy(time = nextTime)
  }
}
