package hydrocul.kametools.web;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ThreadedRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gargoylesoftware.htmlunit.util.WebResponseWrapper;

import hydrocul.kametools.ObjectBank;

class WebSite {

  def matchUrl(url: String): Boolean = true;

  def open(ua: WebClient, url: String, query: String): Any = {

    ua.setRefreshHandler(new ThreadedRefreshHandler(){
      override def handleRefresh(page: Page, url: java.net.URL, requestedWait: Int){
        println(url);
        super.handleRefresh(page, url, requestedWait);
      }
    });

    ua.setJavaScriptEnabled(isJavaScriptEnabled);

    val html: Page = ua.getPage(url);
    if(isJavaScriptEnabled){
      ua.waitForBackgroundJavaScript(1000);
    }

    html match {
      case html: HtmlPage => query match {
        case "" => html.asText;
        case "--text" => html.asText;
        case "--xml" => html.asXml;
        case "--source" => html.getWebResponse.getContentAsString;
        case _ => doQuery(html, query);
      }
    }

  }

  protected def isJavaScriptEnabled: Boolean = false;

  protected def doQuery(html: HtmlPage, query: String): Any = {
    throw new Exception("Unknown query: " + query);
  }

}

object WebSite {

  lazy val default = new WebSite(){}

  def list = default :: Nil;

}

/*
  private def getWebConnection(src: WebConnection): WebConnection = {
    new WebConnectionWrapper(src){
      override def getResponse(request: WebRequest): WebResponse = {
        val res = super.getResponse(request);
        request.getUrl.toString match {
          case _ => res;
        }
//        new WebResponseWrapper(super.getResponse(request)){
//        }
      }
    }
  }
*/

