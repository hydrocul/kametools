package hydrocul.kametools;

import java.io.File;

case class AutoLauncher(downloadDir: String) extends App {

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
    val openingFile = openingFiles.head;
    OpenApp.openDefault(openingFile, env);
    openingFile :: files;
  }

  private def getExistingFiles(): List[File] =
    (new File(downloadDir)).listFiles().toList;

}

object AutoLauncher {
}
