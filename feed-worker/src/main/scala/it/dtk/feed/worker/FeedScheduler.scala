package it.dtk.feed.worker

import scala.concurrent.duration.{FiniteDuration, _}

/**
 * Created by fabiofumarola on 15/08/15.
 */
class FeedScheduler(val initialTime: FiniteDuration = 10 minutes,
  val delta: FiniteDuration = 2 minute) {

  private var lastTime = initialTime
  private val minTime = 4 minutes

  def gotException() = {
    lastTime *= 2
  }

  def whenFake(numNewUrls: Int = -1): FiniteDuration = {
    10 second
  }

  def when(numNewUrls: Int = -1): FiniteDuration = {
    numNewUrls match {
      case -1 => lastTime

      case x: Int if x > 5 =>
        val newTime = lastTime - delta
        lastTime = if (newTime < minTime) minTime else newTime
        lastTime

      case x: Int if x <= 5 =>
        lastTime = lastTime + delta
        lastTime
    }
  }
}
