import java.net.URL

import com.rometools.rome.io.{XmlReader, SyndFeedInput}

/**
 * Created by fabiofumarola on 08/08/15.
 */
object RssRomeExample extends App {

  val url = new URL("http://www.baritoday.it/rss")

  val feedInput = new SyndFeedInput()
  val feed = feedInput.build(new XmlReader(url))

  println(feed)
}
