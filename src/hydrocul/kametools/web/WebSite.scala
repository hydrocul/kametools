package hydrocul.kametools.web;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hydrocul.kametools.ObjectBank;

trait WebSite {

  def urlMatch(url: String): Boolean = true;

  def isJavaScriptEnabled: Boolean = false;

  def createPage(html: HtmlPage): WebPage = new WebPage(){

    override def getTitle = html.getTitleText;

    override def getContent = html.asText;

    override def getXmlSource = html.asXml;

    override def useObjectBank(ob: ObjectBank): WebPage = this;

  }

}

object WebSite {

  lazy val default = new WebSite(){}

  def list = default :: Nil;

}
