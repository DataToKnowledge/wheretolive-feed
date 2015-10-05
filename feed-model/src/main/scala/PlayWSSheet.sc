import com.ning.http.client.AsyncHttpClientConfig.Builder
import play.api.libs.ws._
import play.api.libs.ws.ning.NingWSClient
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util._
val builder = new Builder()
val WS = new NingWSClient(builder.build())

val url = "http://www.ansa.it/sito/notizie/cronaca/2015/10/02/traffico-anabolizzanti-blitz-a-messina_11c68ea1-c078-4bd7-893a-93dec464da1f.html"
val response = WS.url(url).withFollowRedirects(true).get()
val res = Await.result(response, 300.millis)
res.allHeaders

