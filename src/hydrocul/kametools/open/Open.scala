package hydrocul.kametools.open;

import java.awt.Desktop;
import java.io.File;

import hydrocul.kametools.App;
import hydrocul.kametools.Env;
import hydrocul.kametools.FileSet;
import hydrocul.kametools.ObjectBank;

object Open extends App {

  def main(cmdName: String, args: Array[String]){

    val vd = FileSet.getArgFiles(args, Some("."), false, true);
    val list: Stream[File] = vd.toStream;

    if(!vd.isSingleFile){
      println("ファイルが多すぎです");
    } else {
      val desktop = Desktop.getDesktop;
      list.take(3).foreach { f =>
        println("open " + f.getCanonicalPath);
        desktop.open(f);
      }
    }

  }

  def help(cmdName: String){
    println(cmdName); // TODO
  }

}
