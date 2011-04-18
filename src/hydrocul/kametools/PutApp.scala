package hydrocul.kametools;

import java.io.File;

case class PutApp(fileSet: FileSet, target: SyncTarget) extends App {

  override def exec(env: App.Env){
    val file = fileSet.head;
    val name = file.getName;
    val syncFile = SyncFile(file, target);
    val syncFiles = ObjectBank.default.getOrElse("_syncFiles", Vector.empty[SyncFile]);
    val newSyncFiles = syncFiles :+ syncFile;
    ObjectBank.default.put("_syncFiles", Some(newSyncFiles));
    val script = "rsync " + target.rsyncOption + " " +
      App.escapeFilePath(file.getAbsolutePath) +
      " " + App.escapeFilePath(target.targetDir + name);
    env.out.println(script);
    env.shellScriptWriter.println(script);
  }

}

