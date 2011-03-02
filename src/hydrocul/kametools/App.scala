package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(env: App.Env){
    App.HelpApp(this).exec(env);
  }

  def next(arg: String): Option[Any] = {
    nextCommonly(arg);
  }

  protected def nextCommonly(arg: String): Option[Any] = {
    arg match {
      case "--help" => Some(App.HelpApp(this));
      case "--label" => Some(App.NeedOfArgumentApp(arg =>
        Some(App.LabelApp(this, arg))));
      case _ => None;
    }
  }

}

object App {

  def apply(obj: Any): App = {
    obj match {
      case obj: App => obj;
      case obj: FileSet => LsApp(obj);
      case obj: File => LsApp(FileSet(obj));
      case obj => PrintApp(obj);
    }
  }

  object StartApp extends App with java.io.Serializable {

    override def exec(env: App.Env){
      env.out.println("no argument"); // TODO
    }

    override def next(arg: String): Option[Any] = {
      val c = nextCommonly(arg);
      if(c.isDefined){
        c;
      } else if(arg.startsWith("http://") || arg.startsWith("https://")){
        Some(web.WebBrowserApp(arg, None));
      } else if(arg.startsWith("./")){
        val file = (new File(arg.substring(2))).getAbsoluteFile;
        Some(file);
      } else if(arg.startsWith("/") || arg.startsWith("../")){
        val file = (new File(arg)).getAbsoluteFile;
        Some(file);
      } else {
        val o = ObjectBank.default.get(arg);
        if(o.isDefined){
          o;
        } else {
          None;
        }
      }
    }

  }

  case class SimpleApp(p: App.Env=>Unit) extends App {

    override def exec(env: App.Env){
      p(env);
    }

  }

  case class NeedOfArgumentApp(p: String => Option[Any]) extends App {

    override def exec(env: App.Env){
      throw new Exception("need argument");
    }

    override def next(arg: String): Option[Any] = p(arg);

  }

  case class LabelApp(app: App, label: String) extends App {

    override def exec(env: App.Env){
      ObjectBank.default.put(label, Some(app));
    }

  }

  case class HelpApp(app: App) extends App {

    override def exec(env: App.Env){
      env.out.println(app.toString);
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
