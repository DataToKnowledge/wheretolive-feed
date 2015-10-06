import java.net.URL
import java.nio.charset.Charset

import com.google.common.io.Resources
import com.intenthq.gander.Gander

val url = "http://www.ansa.it/sito/notizie/cronaca/2015/10/03/lager-animali-e-armi-in-casa-arrestato_804dc149-1a38-4231-a048-edc22835a9da.html"

val rawHTML = Resources.toString(new URL(url),Charset.forName("utf-8"))

val result = Gander.extract(rawHTML)

val pageInfo = result.get


pageInfo.cleanedText
pageInfo.lang
pageInfo.links
pageInfo.publishDate
pageInfo.metaDescription
pageInfo.metaKeywords
pageInfo.openGraphData
pageInfo.processedTitle
pageInfo.title