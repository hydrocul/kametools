package hydrocul.kametools;

case class AutoLauncher(downloadDir: String) extends App {

  override def exec(env: App.Env){
    while(true){
      waitSub(env);
      Thread.sleep(500);
    }
  }

  private def waitSub(env: App.Env){
/*
    val fileNames = (new File(downloadDir)).listFiles.toSeq.flatMap(_ match {
      case pattern(n1, n2) => Some(n1 + n2);
      case _ => None;
    });
    println(fileNames);
*/
  }

/*
  private val pattern = "(.*)-ktopen(.[^.]+)".r;
*/

}

object AutoLauncher {
}
