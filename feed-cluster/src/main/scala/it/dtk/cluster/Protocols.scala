package it.dtk.cluster

import akka.actor.ActorRef
import it.dtk.feed.Model.FeedInfo

/**
 * Created by fabiofumarola on 18/08/15.
 */
class Protocols {

}

object FrontendMasterProtocol {

  case class AddFeed(source: FeedInfo)
  case class DeleteFeed(source: FeedInfo)
  case class Result(msg: String)
  case class ListFeeds(data: Map[String, FeedInfo] = Map.empty)
  case object ListWorkers
  case class WorkersList(list: List[String])
  case class FeedFailed(f: FeedInfo, ex: Throwable)
  case object Snapshot
  case object EvaluateFeeds
}
