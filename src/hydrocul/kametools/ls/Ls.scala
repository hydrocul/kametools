package hydrocul.kametools.ls;

import java.io.File;

import scala.annotation.tailrec;

import hydrocul.util.StringLib;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object Ls extends App {

  def main(cmdName: String, args: Array[String], env: App.Env){

    val list: Seq[File] = App.getArgFiles(args, true, true, false, true, env);

    var map = env.objectBank.getOrElse[Map[File, String]]("$files", Map());

    def createRandom(len: Int): String = {
      val ret = new StringBuilder();
      (1 to len).foreach { _ =>
        val r = (math.random * 26).asInstanceOf[Int];
        ret.append(('a' + r).asInstanceOf[Char]);
      }
      ret.toString;
    }

    list.foreach { f: File =>
      @tailrec
      def createName(): String = {
        val r = createRandom(3);
        env.objectBank.load("$" + r) match {
          case None => r;
          case Some(_) => createName();
        }
      }
      val name = {
        map.get(f) match {
          case Some(s) => s;
          case None =>
            val s = createName();
            map = map + (f -> s);
            s;
        }
      }
      env.objectBank.put("$" + name, "java.io.File", f);
      println("%s %s".format(name, f));
    }

    env.objectBank.put("$files", "Map[java.io.File, String]", map);

  }

  def help(cmdName: String){
    // TODO
  }

}
