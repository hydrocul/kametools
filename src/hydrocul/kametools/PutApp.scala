package hydrocul.kametools;

import java.io.File;

case class PutApp(fileSet: FileSet, target: SyncTarget) extends App {

  override def exec(env: App.Env){
    val file = fileSet.head;
    val name = file.getName();
    val nameOnTarget = createRemoteFileName(name);
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

  private def createRemoteFileName(name: String): String = {
    val ret = new StringBuilder();
    (1 to 4).foreach { _ =>
      val r = (math.random * 26).asInstanceOf[Int];
      ret.append(('a' + r).asInstanceOf[Char]);
    }
    ret.append((new java.util.Date()).getTime);
    val Pattern = ".*(\\.[a-zA-Z0-9]+)".r;
    name match {
      case Pattern(ext) => ret.append(ext);
      case _ => ;
    }
    ret.toString;
  }

}

object PutApp {

  def getTarget(name: String): Option[SyncTarget] = {
    val targets = ObjectBank.default.getOrElse("_syncTargets", Map.empty[String, SyncTarget]);
    targets.get(name);
  }

}
