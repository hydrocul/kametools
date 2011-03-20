package hydrocul.kametools;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

trait App {

  def exec();

}

object App {

  def toApp(obj: Any): App = {
    obj match {
//      case obj: File => LsApp(FileSet.OneFileSet(obj));
//      case obj: FileSet => LsApp(obj);
      case _ => new App {
        override def exec(){
          env.out.println(obj);
        }
      }
    }
  }

  def env: Env = _env;

  private var _env: Env = new StandardEnv();

  // for test
  def setEnv(env_ : Env){
    _env = env_;
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
