package it.dtk.feed

import com.rometools.rome.feed.synd.SyndEntry
import it.dtk.feed.Model.Feed
import org.joda.time.DateTime
import scala.collection.JavaConversions._


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