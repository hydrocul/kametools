package hydrocul.kametools;

import java.io.File;

case class PullApp(syncFile: SyncFile) extends App {

  override def exec(env: App.Env){
    val file = syncFile.file;
    val name = file.getName;
    val target = syncFile.target;
    val script = "rsync -tv " +
      (if(target.sshOption.isEmpty) "" else "-e \"ssh " + target.sshOption + "\"") +
      " " + App.escapeFilePath(target.targetDir + name) + 
      " " + App.escapeFilePath(file.getAbsolutePath);
    env.shellScriptWriter.println(script);
  }

}

object PullApp {

  def create(args: List[String]): App = {
    args match {
      case syncFile :: Nil =>
        ObjectBank.default.get(syncFile) match {
          case Some(syncFile: SyncFile) => PullApp(syncFile);
          case _ => throw new Exception("Unknown arguments: " + args);
        }
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

}
