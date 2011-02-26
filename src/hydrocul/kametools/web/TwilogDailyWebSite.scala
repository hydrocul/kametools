package hydrocul.kametools.web;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

class TwilogDailyWebSite extends WebSite {

  private val UrlPattern = "http://twilog.org/([a-z0-9_]+)/date-(\\d{6})".r;

  override def matchUrl(url: String): Boolean = url match {
    case UrlPattern(uid, date) => true;
    case _ => false;
  }

  override protected def doQuery(html: HtmlPage, query: String): Any = {
    query match {
      case "--tweets" => getTweetsFromXml(html);
      case _ => super.doQuery(html, query);
    }
  }

  private def getTweetsFromXml(html: HtmlPage): List[String] = {

    val doc = WebSite.html2xml(html);
    (doc \\ "p").filter(e => (e \ "@class").text=="tl-text").map({ tw =>
      tw.child.map(_.text.trim).mkString;
    }).toList;

  }

}
