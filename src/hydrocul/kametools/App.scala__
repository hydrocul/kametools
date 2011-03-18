package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(env: App.Env);

}

object App {

/*
  def next(obj: Any, arg: String, env: App.Env): Any = {
    (obj, arg) match {
      case (app: App, arg) => app.modify(arg) match {
        case Some(next) => next;
        case None => next(app.exec(env), arg, env);
      }
      case (StartApp, arg) if(arg.startsWith("http://") ||
        arg.startsWith("https://")) =>
        web.WebBrowserApp(arg);
      case (StartApp, arg) if(arg == ".") =>
        (new File(".")).getAbsoluteFile;
      case (StartApp, arg) if(arg.startsWith("./")) =>
        (new File(arg.substring(2))).getAbsoluteFile;
      case (StartApp, arg) if(arg.startsWith("../") || arg.startsWith("/")) =>
        (new File(arg)).getAbsoluteFile;
      case (StartApp, "daemon") =>
        OpenApp.FileOpenReceiver;
      case (StartApp, arg) => ObjectBank.default.get(arg) match {
        case Some(o) => o;
        case None => throw new Exception("Unknown object: " + arg);
      }
      case _ => toApp(obj) match {
        case Some(app) => next(app, arg, env);
        case None => throw new Exception("Unknown argument: " + arg);
      }
    }
  }
*/

  def finish(obj: Any, env: App.Env){
    obj match {
      case app: App => app.exec(env);
      case StartApp => throw new Exception("No argument");
      case obj => toApp(obj).exec(env);
    }
  }

  private def toApp(obj: Any): App = {
    obj match {
      case obj: File => LsApp(FileSet.OneFileSet(obj));
      case obj: FileSet => LsApp(obj);
      case _ => new App {
        override def exec(env: App.Env){
          env.out.println(obj);
        }
      }
    }
  }

  object StartApp extends java.io.Serializable;

/*
  case class SimpleApp(p: App.Env=>Unit) extends App {

    override def exec(env: App.Env){
      p(env);
    }

  }

  case class NeedOfArgumentApp(p: String => App) extends App {

    override def exec(env: App.Env){
      throw new Exception("need argument");
    }

    override def modify(arg: String): Option[App] = Some(p(arg));

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
