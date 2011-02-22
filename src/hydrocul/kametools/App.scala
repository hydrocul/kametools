package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(env: App.Env);

  def next(arg: String): App = {
    val c = nextCommonly(arg);
    if(c.isDefined){
      c.get;
    } else {
      throw new Exception("Unknown command: " + arg);
    }
  }

  protected def nextCommonly(arg: String): Option[App] = {
    arg match {
      case "--help" => Some(App.SimpleApp(env => env.out.println(toString)));
      case "--label" => Some(App.NeedOfArgumentApp(arg => App.LabelApp(this, arg)));
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

    override def next(arg: String): App = {
      val c = nextCommonly(arg);
      if(c.isDefined){
        c.get;
      } else if(arg.startsWith("./")){
        val file = (new File(arg.substring(2))).getAbsoluteFile;
        App.apply(file);
      } else if(arg.startsWith("/") || arg.startsWith("../")){
        val file = (new File(arg)).getAbsoluteFile;
        App.apply(file);
      } else {
        val o = ObjectBank.default.get(arg);
        if(o.isDefined){
          App.apply(o.get);
        } else {
          super.next(arg);
        }
      }
    }

  }

  case class SimpleApp(p: App.Env=>Unit) extends App {

    override def exec(env: App.Env){
      p(env);
    }

  }

  case class NeedOfArgumentApp(p: String => App) extends App {

    override def exec(env: App.Env){
      throw new Exception("need argument");
    }

    override def next(arg: String): App = p(arg);

  }

  case class LabelApp(app: App, label: String) extends App {

    override def exec(env: App.Env){
      ObjectBank.default.put(label, Some(app));
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
