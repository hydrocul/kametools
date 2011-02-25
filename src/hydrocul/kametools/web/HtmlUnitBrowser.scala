package hydrocul.kametools.web;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import com.gargoylesoftware.htmlunit.util.WebResponseWrapper;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

case class HtmlUnitBrowser(url: String, printXml: Boolean) extends App {

  override def exec(env: App.Env){

    val ua = new WebClient();

    val ws = WebSite.list.find(_.urlMatch(url)).get;

    ua.setJavaScriptEnabled(ws.isJavaScriptEnabled);

    val html: Page = ua.getPage(url);
    if(ws.isJavaScriptEnabled){
      ua.waitForBackgroundJavaScript(1000);
    }
    if(printXml){
      val page = html match {
        case html: HtmlPage =>
          env.out.println(html.asXml);
      }
    } else {
      val page = html match {
        case html: HtmlPage =>
          ws.createPage(html);
      }
      env.out.println(page.getTitle);
      env.out.println(page.getContent);
    }

  }

  override def next(arg: String): App = (nextCommonly(arg), arg) match {
    case (Some(app), _) => app;
    case (None, "--xml") => HtmlUnitBrowser(url, true);
    case _ => throw new Exception("Unknown option: " + arg);
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
