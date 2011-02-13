package hydrocul.kametools;

import java.io.File;

object Main {

/*
  private val apps: Map[String, App] = Map(
    "now" -> now.Now, TODO
    "ls" -> ls.Ls,
    "ll" -> ls.Ls,
    "open" -> open.Open, TODO
    "print" -> print.Print,
    "web" -> web.HtmlUnitBrowser, TODO
    "groovy" -> groovyevaluator.EvaluateGroovy, TODO
    "scala" -> scalaevaluator.EvaluateScala, TODO
    "help" -> Help
  );
*/

  def main(args: Array[String]){

    // System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    System.setProperty("org.apache.commons.logging.simplelog.showlogname", "true");
    System.setProperty("org.apache.commons.logging.simplelog.showShortLogname", "false");
    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
    System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "warn");

    val env = new App.StandardEnv();
    App.StartApp.main(args, env);

  }

}
