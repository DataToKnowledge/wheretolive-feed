package it.dtk.cluster

import it.dtk.feed.Model.FeedInfo

/**
 * Created by fabiofumarola on 18/08/15.
 */
class Protocols {

}

object FrontendMasterProtocol {

  case class AddFeed(source: FeedInfo)
  case class DeleteFeed(id: String)
  case class Result(msg: String)
  case class ListFeeds(data: Map[String, FeedInfo] = Map.empty)
  case class FeedFailed(f: FeedInfo, ex: Throwable)
}
