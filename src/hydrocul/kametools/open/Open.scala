package hydrocul.kametools.open;

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
      list.take(3).foreach { f =>
        System.getProperty("kt.platform") match {
          case "Windows" => openWindows(f);
          case "Cygwin" => openCygwin(f);
          case "Mac" => openMac(f);
          case "Gnome" => openGnome(f);
          case _ => cannotOpen();
        }
      }
    }

  }

  private def openWindows(f: File){
    val pb = new ProcessBuilder("cmd.exe", "/C", "start", f.getCanonicalPath);
    pb.start();
    println("open " + f.getCanonicalPath);
  }

  private def openCygwin(f: File){
    openWindows(f);
  }

  private def openMac(f: File){
    val pb = new ProcessBuilder("open", f.getCanonicalPath);
    pb.start();
    println("open " + f.getCanonicalPath);
  }

  private def openGnome(f: File){
    val pb = new ProcessBuilder("gnome-open", f.getCanonicalPath);
    pb.start();
    println("open " + f.getCanonicalPath);
  }

  private def cannotOpen(){
    println("このプラットフォームではファイルを開くことができません。");
  }

  def help(cmdName: String){
    // TODO
  }

}
