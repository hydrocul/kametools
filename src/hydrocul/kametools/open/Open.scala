package hydrocul.kametools.open;

import java.awt.Desktop;
import java.io.File;

import hydrocul.kametools.App;
import hydrocul.kametools.Env;
import hydrocul.kametools.ObjectBank;
import hydrocul.kametools.VirtualDirectory;

object Open extends App {

  def main(cmdName: String, args: Array[String], env: Env){

    val vd = VirtualDirectory.getArgFiles(args, Some("."),
      false, true, env);
    val list: Stream[File] = vd.getList;

    if(list.size > 3){
      println("ファイルが多すぎです");
    } else {
      val desktop = Desktop.getDesktop;
      list.take(3).foreach { f =>
        desktop.open(f);
      }
    }

  }

  def help(cmdName: String){
    // TODO
  }

}
