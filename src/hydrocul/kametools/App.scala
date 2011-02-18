package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(env: App.Env);

  def next(arg: String): App = {
    throw new Exception("Unknown command: " + arg);
  }

  protected def nextCommonly(arg: String): Option[App] = {
    None; // TODO
  }

}

object App {

  def apply(obj: Any): App = {
    obj match {
      case obj: App => obj;
//      case obj: FileSet => ls.LsApp(obj);
//      case obj: File => ls.LsApp(FileSet(obj));
      case obj => PrintApp(obj);
    }
  }

  object StartApp extends App {

    override def exec(env: App.Env){
      // TODO
    }

    override def next(arg: String): App = {
      val c = nextCommonly(arg);
      if(c.isDefined){
        c.get;
      } else if(arg.startsWith("/") || arg.startsWith("./") || arg.startsWith("../")){
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

  case class NeedOfArgumentApp(p: String => App) extends App {

    override def exec(env: App.Env){
      throw new Exception("need argument");
    }

    override def next(arg: String): App = p(arg);

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
