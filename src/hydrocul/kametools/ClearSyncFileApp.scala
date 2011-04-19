package hydrocul.kametools;

import java.io.File;

case class ClearSyncFileApp(syncFileKey: String) extends App {

  override def exec(env: App.Env){
    ObjectBank.default.get(syncFileKey) match {
      case Some(syncFile: SyncFile) =>
        ObjectBank.default.put(syncFileKey, None);
        val syncFiles = ObjectBank.default.getOrElse("_syncFiles", Vector.empty[SyncFile]);
        val newSyncFiles = syncFiles.filter(_ != syncFile);
        ObjectBank.default.put("_syncFiles", Some(newSyncFiles));
      case _ => ;
    }
  }

}

object ClearSyncFileApp {

  def create(args: List[String]): App = {
    args match {
      case syncFileKey :: Nil => ClearSyncFileApp(syncFileKey);
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

}
