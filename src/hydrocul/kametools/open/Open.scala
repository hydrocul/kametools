package hydrocul.kametools.open;

import java.io.File;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object Open extends App {

  def main(cmdName: String, args: Array[String], env: App.Env){

    val list: Seq[File] = App.getArgFiles(args, true, false, false, true, env);

    if(list.size > 3){
      println("ファイルが多すぎです");
    } else {
      System.getProperty("kt.platform") match {
        case "Windows" => list.foreach { f => openWindows(f); }
        case "Cygwin" => list.foreach { f => openCygwin(f); }
        case "Mac" => list.foreach { f => openMac(f); }
        case "Gnome" => list.foreach { f => openGnome(f); }
        case _ => cannotOpen();
      }
    }

  }

  private def openWindows(f: File){
    val pb = new ProcessBuilder("cmd.exe", "/C", "start", f.getCanonicalPath);
    pb.start();
  }

  private def openCygwin(f: File){
    cannotOpen();
  }

  private def openMac(f: File){
    val pb = new ProcessBuilder("open", f.getCanonicalPath);
    pb.start();
  }

  private def openGnome(f: File){
    val pb = new ProcessBuilder("gnome-open", f.getCanonicalPath);
    pb.start();
  }

  private def cannotOpen(){
    println("このプラットフォームではファイルを開くことができません。");
  }

  def help(cmdName: String){
    // TODO
  }

}
