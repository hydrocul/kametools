package hydrocul.kametools;

case class ShowSyncFilesApp() extends App {

  override def exec(env: App.Env){
    val syncFiles = ObjectBank.default.getOrElse("_syncFiles", Vector.empty[SyncFile]);
    syncFiles.map({sf =>
      val key = ObjectBank.default.put(sf);
      key + ": " + sf.file.getAbsolutePath + " -> " + sf.target + " " + sf.nameOnTarget;
    }).foreach(env.out.println(_));
  }

}

object ShowSyncFilesApp {

  def create(args: List[String]): App = {
    args match {
      case Nil =>
        ShowSyncFilesApp();
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

}

