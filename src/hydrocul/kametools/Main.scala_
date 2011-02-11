package hydrocul.kametools;

import java.io.File;

object Main {

  private val apps: Map[String, App] = Map(
    "now" -> now.Now,
    "ls" -> ls.Ls,
    "ll" -> ls.Ls,
    "open" -> open.Open,
    "print" -> print.Print,
    "web" -> web.HtmlUnitBrowser,
    "groovy" -> groovyevaluator.EvaluateGroovy,
    "scala" -> scalaevaluator.EvaluateScala,
    "help" -> Help
  );

  def main(args: Array[String]){

//    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    System.setProperty("org.apache.commons.logging.simplelog.showlogname", "true");
    System.setProperty("org.apache.commons.logging.simplelog.showShortLogname", "false");
    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
    System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "warn");

    if(System.getProperty("kt.platform")==null){
      if(File.separatorChar=='\\'){
        System.setProperty("kt.platform", "Windows");
      }
    }

    if(args.size == 0){
      printHelp();
    } else {
      val cmd = args(0);
      apps.get(cmd) match {
        case Some(a) =>
          a.main(cmd, args.drop(1));
        case None =>
          println("Unknown command: " + cmd);
          printHelp();
      }
    }
  }

  def printHelp(){
    apps.map(kv => kv._1).foreach { cmdName =>
      println("kt " + cmdName);
    }
  }

  object Help extends App {

    def main(cmdName: String, args: Array[String]){
      args.foreach { cmd =>
        apps.get(cmd) match {
          case Some(a) =>
            a.help(cmd);
          case None =>
            println("Unknown command: " + cmd);
            printHelp();
        }
      }
    }

    def help(cmdName: String){
      println(cmdName); // TODO
    }

  }

}
