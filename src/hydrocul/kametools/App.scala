package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(env: App.Env);

}

object App {

  def exec(args: Array[String]){
    val app = args.toList match {
      case "ls" :: tail => LsApp.create(tail);
      case "open" :: tail => OpenApp.create(tail);
      case "pull" :: tail => PullApp.create(tail);
      case "setSyncTarget" :: tail => SetSyncTargetApp.create(tail);
      case "showSyncFiles" :: tail => ShowSyncFilesApp.create(tail);
      case _ => throw new Exception("Not Found: " + args);
    }
    val env = new StandardEnv();
    app.exec(env);
    println(env.shellScriptCode);
  }

  trait Env {

    def out: PrintWriter;

    def shellScriptWriter: PrintWriter;

  }

  class StandardEnv extends Env {

    override val out: PrintWriter = new PrintWriter(System.out, true);

    private val _shellScriptStringWriter = new StringWriter();

    override val shellScriptWriter = new PrintWriter(_shellScriptStringWriter, true);

    def shellScriptCode = _shellScriptStringWriter.toString;

  }

  class StringEnv extends Env {

    private val sp = new StringWriter();

    override lazy val out: PrintWriter = new PrintWriter(sp, true);

    def output(): String = sp.toString;

    private val _shellScriptStringWriter = new StringWriter();

    override val shellScriptWriter = new PrintWriter(_shellScriptStringWriter, true);

    def shellScriptCode = _shellScriptStringWriter.toString;

  }

  def escapeFilePath(filePath: String): String = "\"" + filePath + "\""; // TODO

}
