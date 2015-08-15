package it.dtk.feed

import com.rometools.rome.feed.synd.SyndEntry
import Model._
import scala.collection.JavaConversions._
import com.github.nscala_time.time.Imports._

/**
 * Created by fabiofumarola on 09/08/15.
 */
object FeedParser {

  def apply(entry: SyndEntry): Feed = {
    Feed(
      title = entry.getTitle,
      description = entry.getDescription.getValue,
      categories = entry.getCategories.map(_.getName).toList,
      imageUrl = entry.getEnclosures.map(_.getUrl).mkString(""),
      uri = entry.getUri,
      date = new DateTime(entry.getPublishedDate))
  }
}