package it.dtk.feed.logic

import java.io.ByteArrayInputStream
import java.net.URL
import java.nio.charset.Charset

import com.intenthq.gander.opengraph.OpenGraphData
import com.intenthq.gander.{ Gander, PageInfo }
import com.ning.http.client.AsyncHttpClientConfig.Builder
import com.rometools.rome.io.{ SyndFeedInput, XmlReader }
import it.dtk.feed.Model.{ Feed, FeedScheduler, _ }
import org.apache.tika.language.LanguageIdentifier
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.txt.TXTParser
import org.apache.tika.parser.{ ParseContext, Parser }
import org.apache.tika.sax.BodyContentHandler
import org.joda.time.DateTime
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ning.NingWSClient

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util._

/**
 * Created by fabiofumarola on 05/10/15.
 */

object FeedUtil {

  type pageText = String
  type lang = String
  type authors = String

  def parseFeed(url: String): mutable.Buffer[Feed] = {
    val reader = new SyndFeedInput()
    val rawFeeds = reader.build(new XmlReader(new URL(url)))

    rawFeeds.getEntries.map { entry =>
      Feed(
        title = entry.getTitle,
        description = entry.getDescription.getValue,
        categories = entry.getCategories.map(_.getName).toList,
        imageUrl = entry.getEnclosures.map(_.getUrl).mkString(""),
        uri = entry.getUri,
        date = new DateTime(entry.getPublishedDate))
    }
  }

  def processFeedEntry(feed: Feed, html: String, contentType: String): ProcessedFeed = {
    val (pageText, lang, authors) = processTika(feed, html, contentType)

    Gander.extract(html) match {

      case Some(pageInfo) =>
        ProcessedFeed(
          uri = feed.uri,
          processedTitle = pageInfo.processedTitle,
          summary = feed.description,
          metaDescription = pageInfo.metaDescription,
          categories = feed.categories,
          metaKeywords = pageInfo.metaKeywords.split(",").map(_.trim).toList,
          imageUrl = feed.imageUrl,
          publishDate = pageInfo.publishDate.map(d => new DateTime(d.getTime)).getOrElse(feed.date),
          language = pageInfo.lang.getOrElse(lang),
          html = html,
          cleanedText = pageInfo.cleanedText.getOrElse(""),
          pageText = pageText,
          authors = authors,
          openGraphData = Some(pageInfo.openGraphData)
        )
      case None =>
        ProcessedFeed(
          uri = feed.uri,
          processedTitle = feed.title,
          summary = feed.description,
          metaDescription = "",
          categories = feed.categories,
          metaKeywords = List.empty,
          imageUrl = feed.imageUrl,
          publishDate = feed.date,
          language = lang,
          html = html,
          cleanedText = "",
          pageText = pageText,
          authors = authors,
          openGraphData = None
        )
    }
  }

  private def getParser(contentType: String): Parser = contentType match {
    case value if value contains "html"  => new HtmlParser
    case value if value contains "plain" => new TXTParser
    case value if value contains "pdf"   => new PDFParser
    case _                               => new HtmlParser
  }

  private def processTika(feed: Feed, html: String, contentType: String): (pageText, lang, authors) = {
    val in = new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8")))
    val metadata = new Metadata
    val bodyHandler = new BodyContentHandler()
    val context = new ParseContext
    val parser = getParser(contentType)

    try {
      parser.parse(in, bodyHandler, metadata, context)
    }
    catch {
      case e: Throwable =>
      //if there is an error we don't care
    }
    finally { in.close() }

    val body = bodyHandler.toString

    (body, new LanguageIdentifier(body).getLanguage, Option(metadata.get("author")).getOrElse(""))
  }

}

class HttpDownloader {
  //check for configurations https://www.playframework.com/documentation/2.4.x/ScalaWS
  private val builder = new Builder()
  builder.setFollowRedirect(true)
  builder.setUserAgent("wheretolive.it")

  val WS = new NingWSClient(builder.build())

  def close() = WS.close()

  def download(url: String)(implicit ec: ExecutionContext): Future[WSResponse] =
    WS.url(url).withFollowRedirects(true).get()
}

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

object Logic
