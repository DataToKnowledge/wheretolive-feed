
import org.jsoup._

val html = "<p>BRINDISI- “Vogliamo potere, una scuola fondata sulla partecipazione comune e sulla democrazia”. Questo è uno dei tanti...</p>\n<p>L'articolo <a rel=\"nofollow\" href=\"http://www.brindisioggi.it/700-studenti-in-piazza-il-rappresentante-delluds-potevamo-essere-di-piu/\">700 studenti in piazza, il rappresentante dell&#8217;UDS: &#8220;Potevamo essere di più&#8221;</a> sembra essere il primo su <a rel=\"nofollow\" href=\"http://www.brindisioggi.it\">Brindisi Oggi, news Brindisi notizie Brindisi e provincia</a>.</p>\n"
val doc = Jsoup.parse(html)

doc.select("a[href]")

doc.text()