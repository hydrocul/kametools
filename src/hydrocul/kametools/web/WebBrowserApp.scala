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

case class WebBrowserApp(url: String, query: Option[String]) extends App {

  override def exec(env: App.Env){

    val q = query match {
      case Some(q) => q;
      case None => "";
    }

    val ua = new WebClient();
    val result = WebSite.list.find(_.matchUrl(url)).get.open(ua, url, q);

    env.out.println(result);

  }

  override def next(arg: String): Option[Any] = {
    val c = nextCommonly(arg);
    if(c.isDefined){
      c;
    } else arg match {
      case newQuery if(!query.isDefined) => Some(WebBrowserApp(url, Some(newQuery)));
      case _ => None;
    }
  }

}

object WebBrowserApp {

}
