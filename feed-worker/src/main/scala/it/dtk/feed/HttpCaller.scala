package it.dtk.feed

import com.ning.http.client.AsyncHttpClientConfig
import play.api.libs.ws.ning.NingWSClient
import scala.concurrent.Future

/**
 * Created by fabiofumarola on 09/08/15.
 */
object HttpCaller {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val builder = new AsyncHttpClientConfig.Builder()
  private val WS = new NingWSClient(builder.build())

  def download(url: String): Future[String] = {
    WS.url(url).withFollowRedirects(true).get().map { response =>
      val content = response.header("Content-Type").getOrElse("")
      response.body
    }
  }
}
