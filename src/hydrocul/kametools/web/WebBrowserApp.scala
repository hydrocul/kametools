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

case class WebBrowserApp(url: String, printXml: Boolean) extends App {

  override def exec(env: App.Env){

    val browser = new WebBrowser();

    val page = browser.open(url);

    if(printXml){
      env.out.println(page.getXmlSource);
    } else {
      val page2 = page.useObjectBank;
      env.out.println(page2.getContent);
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
