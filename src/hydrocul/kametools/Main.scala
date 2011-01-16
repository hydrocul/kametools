package hydrocul.kametools;

import java.io.File;

object Main {

  private val apps: Map[String, App] = Map(
    "now" -> now.Now,
    "ls" -> ls.Ls,
    "ll" -> ls.Ls,
    "open" -> open.Open,
    "print" -> print.Print,
    "groovy" -> groovyevaluator.EvaluateGroovy,
    "scala" -> scalaevaluator.EvaluateScala
  );

  def main(args: Array[String]){

    if(System.getProperty("kt.platform")==null){
      if(File.separatorChar=='\\'){
        System.setProperty("kt.platform", "Windows");
      }
    }

    if(args.size == 0){
      printHelp();
    } else {
      val env = new Env(new ObjectBank(getDirName()));
      val cmd = args(0);
      val app: Option[App] = try {
        Some(apps(cmd));
      } catch {
        case _ => None;
      }
      app match {
        case Some(a) =>
          a.main(cmd, args.drop(1), env);
        case None =>
          println("Unknown command: " + cmd);
          printHelp();
      }
    }
  }

  def printHelp(){
    println("kametools now");
  }

  private def getDirName(): String = {
    import java.io.File;
    System.getProperty("user.home") + File.separator + ".kametools";
  }

}
