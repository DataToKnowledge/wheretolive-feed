import java.net.URL
import java.nio.charset.Charset

import com.google.common.io.Resources
import com.intenthq.gander.Gander

val url = "http://www.brindisioggi.it/?p=76103"

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