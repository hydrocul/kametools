package hydrocul.kametools;

import java.io.File;

case class PutApp(fileSet: FileSet, target: SyncTarget) extends App {

  override def exec(env: App.Env){
    val file = fileSet.head;
    val nameOnTarget = ObjectBank.default.put(fileSet);
    val syncFile = SyncFile(file, target, nameOnTarget);
    val syncFiles = ObjectBank.default.getOrElse("_syncFiles", Vector.empty[SyncFile]);
    val newSyncFiles = syncFiles :+ syncFile;
    ObjectBank.default.put("_syncFiles", Some(newSyncFiles));
    val script = "rsync -tv " +
      (if(target.sshOption.isEmpty) "" else "-e \"ssh " + target.sshOption + "\"") +
      " " + App.escapeFilePath(file.getAbsolutePath) +
      " " + App.escapeFilePath(target.targetDir + nameOnTarget);
    env.shellScriptWriter.println(script);
  }

}

object PutApp {

  def getTarget(name: String): Option[SyncTarget] = {
    val targets = ObjectBank.default.getOrElse("_syncTargets", Map.empty[String, SyncTarget]);
    targets.get(name);
  }

}
