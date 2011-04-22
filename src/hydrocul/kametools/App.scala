package hydrocul.kametools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
      case "clearSyncFile" :: tail => ClearSyncFileApp.create(tail);
      case "cp" :: tail => SvnCpApp.create(tail);
      case "autoLauncher" :: tail => AutoLauncherApp.create(tail);
      case "help" :: tail => HelpApp;
      case args => LsApp.create(args);
    }
    val env = new StandardEnv();
    app.exec(env);
    execShell(env.shellScriptCode, env);
  }

  private def execShell(scriptCode: String, env: Env) = if(!scriptCode.isEmpty){
    env.out.print(scriptCode);
    val process = Runtime.getRuntime.exec("/bin/sh");
    def writeOutput(ip: InputStream){
      val runnable = new Runnable(){
        def run(){
          val buf = new Array[Char](1024);
          val ip2 = new BufferedReader(new InputStreamReader(ip, "UTF-8"));
          var len = ip2.read(buf, 0, buf.length);
          while(len > 0){
            env.out.write(buf, 0, len);
            len = ip2.read(buf, 0, buf.length);
            env.out.flush();
          }
        }
      }
      val thread = new Thread(runnable);
      thread.start();
    }
    writeOutput(process.getInputStream);
    writeOutput(process.getErrorStream);
    val op = process.getOutputStream;
    val op2 = new BufferedWriter(new OutputStreamWriter(op, "UTF-8"));
    try {
      op2.write(scriptCode);
    } finally {
      op2.close();
    }
    val exitCode = process.waitFor;
    env.out.flush();
    if(exitCode!=0){
      env.out.println("exitCode: " + exitCode);
    }
  }

  object HelpApp extends App {

    override def exec(env: Env){
      val msg = List(
        "ls",
        "open",
        "pull",
        "setSyncTarget",
        "showSyncFiles",
        "clearSyncFile",
        "cp",
        "autoLauncher",
        "help"
      );
      msg.foreach(env.out.println(_));
    }

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
