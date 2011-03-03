package hydrocul.kametools;

import java.io.File;

object Main {

  def main(args: Array[String]){

    // System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    System.setProperty("org.apache.commons.logging.simplelog.showlogname", "true");
    System.setProperty("org.apache.commons.logging.simplelog.showShortLogname", "false");
    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
    System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "fatal");

    val env = new App.StandardEnv();

    App.finish(args.foldLeft[Any](App.StartApp)((obj: Any, arg: String) =>
      App.next(obj, arg, env)), env);

  }

}
