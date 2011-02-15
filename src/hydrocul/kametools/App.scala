package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(env: App.Env);

  def next(arg: String): App;

  protected def nextCommonly(arg: String): Option[App] = {
    None; // TODO
  }

  def help(env: App.Env);

}

object App {

/*
  def apply(obj: Any): App = {
    obj match {
      case obj: App => obj;
      case obj: FileSet => ls.LsApp(obj);
      case obj: File => ls.LsApp(FileSet(obj));
      case obj => print.PrintApp(obj);
    }
  }
*/

  object StartApp extends App {

    override def exec(env: App.Env){
      // TODO
    }

    override def next(arg: String): App = {
      throw new Exception(); // TODO
    }

    override def help(env: App.Env){
      // TODO
    }

  }

/*
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
*/

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
