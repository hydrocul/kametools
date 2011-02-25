package hydrocul.kametools.web;

import hydrocul.kametools.ObjectBank;

trait WebPage {

  def getTitle: String;

  def getContent: String;

  def getXmlSource: String;

  def getSource: String;

  def useObjectBank(ob: ObjectBank): WebPage;

  def useObjectBank: WebPage = useObjectBank(ObjectBank.default);

}
