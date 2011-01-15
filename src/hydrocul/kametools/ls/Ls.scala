package hydrocul.kametools.ls;

import java.io.File;

import scala.annotation.tailrec;

import hydrocul.util.StringLib;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object Ls extends App {

  def main(cmdName: String, args: Array[String], env: App.Env){

    val list: Seq[File] = App.getArgFiles(args, true, true, false, true, env);

    var map = env.objectBank.getFiles;

    list.foreach { f: File =>
      val s = env.objectBank.putFile(f, map);
      println("%s %s".format(s._1, f));
      map = s._2;
    }

    env.objectBank.putFiles(map);

  }

  def help(cmdName: String){
    // TODO
  }

}
