package hydrocul.kametools;

import java.io.File;

case class AutoLauncherApp(downloadDir: String) extends App {

  override def exec(env: App.Env){
    var files = getExistingFiles();
    while(true){
      files = waitSub(env, files);
      Thread.sleep(500);
    }
  }

  private def waitSub(env: App.Env, files: List[File]): List[File] = {
    val newFiles = getExistingFiles();
    val openingFiles = newFiles.diff(files);
    if(openingFiles.isEmpty){
      files;
    } else {
      val openingFile = openingFiles.head;
      if(openingFile.getName.startsWith(".")){
        files;
      } else {
        OpenApp.openDefault(openingFile, env);
        openingFile :: files;
      }
    }
  }

  private def getExistingFiles(): List[File] =
    (new File(downloadDir)).listFiles().toList;

}

object AutoLauncherApp {

  def create(args: List[String]): AutoLauncherApp = {
    args match {
      case downloadDir :: Nil =>
        AutoLauncherApp(downloadDir);
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

}
