package hydrocul.kametools;

case class SetSyncTargetApp(name: String, targetDir: String, sshOption: String) extends App {

  override def exec(env: App.Env){
    val targets = ObjectBank.default.getOrElse("_syncTargets", Map.empty[String, SyncTarget]);
    val newTargets = targets + (name -> SyncTarget(targetDir, sshOption));
    ObjectBank.default.put("_syncTargets", Some(newTargets));
  }

}

object SetSyncTargetApp {

  def create(args: List[String]): SetSyncTargetApp = {
    args match {
      case name :: targetDir :: Nil =>
        SetSyncTargetApp(name, targetDir, "");
      case name :: targetDir :: sshOption :: Nil =>
        SetSyncTargetApp(name, targetDir, sshOption);
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

}

