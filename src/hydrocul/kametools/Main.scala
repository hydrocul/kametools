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

    val app = args.foldLeft[AnyRef](Filter.StartApp)((obj: AnyRef, arg: String) =>
      Filter.next(obj, arg));
    Filter.finish(app);

  }

}
