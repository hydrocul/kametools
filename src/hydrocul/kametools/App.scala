package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(args: Array[String], env: App.Env);

  def help(env: App.Env);

  final def main(args: Array[String], env: App.Env){

    if(args.isEmpty){
      exec(args, env);
    } else {
      args.head match {
        case _ => exec(args, env);
      }
    }

  }

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

    override lazy val out: PrintWriter = new PrintWriter(System.out, true);

  }

  class StringEnv extends Env {

    private val sp = new StringWriter();

    override lazy val out: PrintWriter = new PrintWriter(sp, true);

    def getOutput(): String = sp.toString;

  }

}
