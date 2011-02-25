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

class WebBrowser {

  private val ua = new WebClient();
  ua.setRefreshHandler(new ThreadedRefreshHandler(){
    override def handleRefresh(page: Page, url: java.net.URL, requestedWait: Int){
      println(url);
      super.handleRefresh(page, url, requestedWait);
    }
  });

  def open(url: String): WebPage = {

    val ws = WebSite.list.find(_.urlMatch(url)).get;

    ua.setJavaScriptEnabled(ws.isJavaScriptEnabled);

    val html: Page = ua.getPage(url);
    if(ws.isJavaScriptEnabled){
      ua.waitForBackgroundJavaScript(1000);
    }
    val page: WebPage = html match {
      case html: HtmlPage =>
        ws.createPage(html);
    }

    page;

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

}
