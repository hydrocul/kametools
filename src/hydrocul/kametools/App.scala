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
        case "--help" => HelpApp(this).main(args.tail, env);
        case _ => exec(args, env);
      }
    }

  }

}

object App {

  def apply(obj: Any): App = {
    obj match {
      case obj: App => obj;
      case obj: FileSet => ls.LsApp(obj);
      case obj: File => ls.LsApp(FileSet(obj));
      case obj => print.PrintApp(obj);
    }
  }

  object StartApp extends App {

    override def exec(args: Array[String], env: App.Env){

      if(args.isEmpty){
        help(env);
        return;
      }

      val t = getNextApp(args);
      val nextApp: App = t._1;
      val nextArgs: Array[String] = t._2;

      nextApp.main(nextArgs, env);

    }

    override def help(env: App.Env){
      // TODO
    }

  }

  private def getNextApp(args: Array[String]): (Any, Array[String]) = {

    val arg = args.head;

    if(arg.startsWith("http://") || arg.startsWith("https://")){
      throw new Exception("TODO: handling url");
    } else {

      val file = (new File(arg)).getCanonicalFile;

      ObjectBank.default.get(arg) match {
        case None =>
          if(file.exists){
            Some(file);
          } else {
            None;
          }
        case Some(f) =>
          if(file.exists){
            throw new Exception("duplicated: " + arg);
          } else {
            Some(f);
          }
      }

      作りかけ

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
