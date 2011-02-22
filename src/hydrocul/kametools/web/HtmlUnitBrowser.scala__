package hydrocul.kametools.web;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.{ Option => CliOption }
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
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

object HtmlUnitBrowser extends App {

  def main(cmdName: String, args: Array[String]){

    val options = getOptions();

    val parser = new PosixParser();
    val cli = try {
      parser.parse(options, args);
    } catch {
      case e: ParseException =>
        System.err.println(e.getMessage);
        return;
    }

    val textOnly: Boolean = cli.hasOption("t");

    val url = cli.getArgs.head;

    val ua = new WebClient();

    ua.setJavaScriptEnabled(isJavaScriptEnabled(url));

    val html: Page = ua.getPage(url);
    ua.waitForBackgroundJavaScript(1000);
    html match {
      case html: HtmlPage =>
        if(textOnly){
          println(html.asText);
        } else {
          println(html.asXml);
        }
    }

  }

  def help(cmdName: String){
    val formatter = new HelpFormatter();
    formatter.printHelp("ls", getOptions());
  }

  private def getOptions(): Options = {
    val options = new Options();
    options.addOption("t", false, "display text only");
    options;
  }

  private val TwitterUrlPattern = "http://twitter\\.com.*".r;

  private def isJavaScriptEnabled(url: String): Boolean = {
    url match {
      case TwitterUrlPattern() => false;
      case _ => true;
    }
  }

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

}
