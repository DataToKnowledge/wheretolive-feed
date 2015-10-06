package it.dtk.feed

import com.intenthq.gander.opengraph.OpenGraphData
import org.joda.time.DateTime

import scala.concurrent.duration.{ FiniteDuration, _ }

/**
 * Created by fabiofumarola on 08/08/15.
 */
object Model {

  case class FeedScheduler(time: FiniteDuration = 10 minutes,
                           delta: FiniteDuration = 2 minutes)

  case class FeedSource(url: String)

  case class FeedInfo(url: String,
                      added: Long,
                      last100Urls: List[String] = List.empty,
                      countUrl: Long = 0,
                      fScheduler: FeedScheduler = FeedScheduler())

  case class Feed(title: String,
                  description: String,
                  categories: List[String],
                  imageUrl: String,
                  date: DateTime,
                  uri: String)

  case class ProcessedFeed(
    uri: String,
    processedTitle: String,
    summary: String,
    metaDescription: String,
    categories: List[String],
    metaKeywords: List[String],
    imageUrl: String,
    publishDate: DateTime,
    language: String,
    html: String,
    cleanedText: String,
    pageText: String,
    authors: String,
    openGraphData: Option[OpenGraphData])

  //FIXME add when processing Text
  //  case class NlpFeed(
  //    feed: ProcessedFeed,
  //    nlp: Any,
  //    ner: Any,
  //    tags: Any)
  //
  //  case class Nlp(
  //    title: List[Token],
  //    description: List[Token],
  //    content: List[Token])
  //
  //  case class Token()
  //
  //  case class LocalizedFeed(
  //    feed: NlpFeed,
  //    focusLocation: Location,
  //    focusDate: String)
  //
  //  case class Location(
  //    city_name: String,
  //    province_name: String,
  //    region_name: String,
  //    population: String,
  //    wikipedia_url: String,
  //    geoname_url: String,
  //    geo_location: String)
}
