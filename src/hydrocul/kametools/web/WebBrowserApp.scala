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

case class WebBrowserApp(url: String, mode: WebBrowserApp.OutputMode) extends App {

  override def exec(env: App.Env){

    val browser = new WebBrowser();

    val page = browser.open(url);

    mode match {
      case WebBrowserApp.TextMode =>
        val page2 = page.useObjectBank;
        env.out.println(page2.getContent);
      case WebBrowserApp.XmlMode =>
        env.out.println(page.getXmlSource);
      case WebBrowserApp.SourceMode =>
        env.out.println(page.getSource);
    }

  }

  override def next(arg: String): App = (nextCommonly(arg), arg) match {
    case (Some(app), _) => app;
    case (None, "--xml") => WebBrowserApp(url, WebBrowserApp.XmlMode);
    case (None, "--source") => WebBrowserApp(url, WebBrowserApp.SourceMode);
    case _ => throw new Exception("Unknown option: " + arg);
  }

}

object WebBrowserApp {

  abstract sealed class OutputMode extends java.io.Serializable;

  object TextMode extends OutputMode;

  object XmlMode extends OutputMode;

  object SourceMode extends OutputMode;

}
