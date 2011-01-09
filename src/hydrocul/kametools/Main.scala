package hydrocul.kametools;

object Main {

  private val apps: Map[String, App] = Map(
    "now" -> now.Now,
    "ls" -> ls.Ls,
    "ll" -> ls.Ls,
    "open" -> open.Open
  );

  def main(args: Array[String]){
    if(args.size == 0){
      printHelp();
    } else {
      val env = new App.Env(new ObjectBank(getDirName()));
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
