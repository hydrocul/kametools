package hydrocul.kametools;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import scala.collection.immutable.Queue;

case class SetSyncTargetApp(name: String, targetDir: String, sshOption: String) extends App {

  override def exec(env: App.Env){
    val targets = ObjectBank.default.getOrElse("_syncTargets", Map.empty[String, SyncTarget]);
    val newTargets = targets + (name -> SyncTarget(targetDir, sshOption));
    ObjectBank.default.put("_syncTargets", Some(newTargets));
  }

}

object SetSyncTargetApp {

  def create(args: List[String]): App = {
    args match {
      case name :: targetDir :: Nil =>
        SetSyncTargetApp(name, targetDir, "");
      case name :: targetDir :: sshOption :: Nil =>
        SetSyncTargetApp(name, targetDir, sshOption);
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

}

