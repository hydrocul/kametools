package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec(env: App.Env): Any;

  def modify(arg: String): Option[Any] = None;

}

object App {

  def next(obj: Any, arg: String, env: App.Env): Any = {
    (obj, arg) match {
      case (app: App, arg) => app.modify(arg) match {
        case Some(next) => next;
        case None => next(app.exec(env), arg, env);
      }
      case _ => throw new Exception("Unknown argument: " + arg);
    }
  }

  def finish(obj: Any, env: App.Env){
    obj match {
      case app: App => app.exec(env);
      case obj => finishDefault(obj, env);
    }
  }

  private def finishDefault(obj: Any, env: App.Env){
    env.out.println(obj);
  }

  object StartApp extends App with java.io.Serializable {

    override def exec(env: App.Env){
      env.out.println("no argument"); // TODO
    }

    override def modify(arg: String): Option[Any] = {
/* TODO
      if(arg.startsWith("http://") || arg.startsWith("https://")){
        Some(web.WebBrowserApp(arg, None));
      } else
*/
      if(arg.startsWith("./")){
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
