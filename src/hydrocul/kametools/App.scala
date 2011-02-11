package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def main(cmdName: String, args: Array[String], env: App.Env);

  def help(cmdName: String, env: App.Env);

}

object App {

  def getApp(obj: Any){
    obj match {
      case obj: App => obj;
      case obj => print.Print(obj);
    }
  }

  trait Env {

    def out: PrintWriter;

  }

  class StandardEnv extends Env {

    override lazy val out: PrintWriter = new PrintWriter(System.out);

  }

  class StringEnv extends Env {

    private val sp = new StringWriter();

    override lazy val out: PrintWriter = new PrintWriter(System.out, true);

    def getOutput(): String = sp.toString;

  }

}
