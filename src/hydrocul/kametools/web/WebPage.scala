package hydrocul.kametools.web;

import hydrocul.kametools.ObjectBank;

trait WebPage {

  def getTitle: String;

  def getContent: String;

  def useRelatedObject(ob: ObjectBank): WebPage;

}
