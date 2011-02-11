package hydrocul.kametools.open;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import hydrocul.kametools.App;
import hydrocul.kametools.FileSet;
import hydrocul.kametools.ObjectBank;

object Open extends App {

  def main(cmdName: String, args: Array[String]){

    val vd = FileSet.getArgFiles(args, Some("."), false, true);

    if(!vd.isSingleFile){
      println("ファイルが多すぎです");
    } else {
      openFile(vd.head);
    }

  }

  def help(cmdName: String){
    println(cmdName); // TODO
  }

  private def openFile(file: File){

    val openerList = ObjectBank.getOrElse[String]("opener", "");

    val defaultDir = ObjectBank.dirName;

println(openerList);
    val path = file.getAbsolutePath;
println(path);
    val opener: Option[(String, String, String)] = openerList.split("\n").
      filter(!_.isEmpty).find( line => {
        // line には、正規表現、パスの先頭文字列、書き換え先パスの先頭文字列、
        // ファイル保存先ディレクトリ、文字コードの順にコンマ区切りで保存されている
        val d = line.split(",");
println(d.toList);
        if(d.size < 3){
          false;
        } else if(!path.startsWith(d(1))){
          false;
        } else if(d(0).isEmpty){
          true;
        } else {
          try {
            val p = java.util.regex.Pattern.compile(d(0))
            val m = p.matcher(path);
            m.matches();
          } catch {
            case e => e.printStackTrace();
              false;
          }
        }
      }) match {
        case None => None;
        case Some(line) => {
          val d = line.split(",");
          val p1 = d(2) + path.substring(d(1).size);
          val p2 = if(d.size <= 3) defaultDir else d(3);
          val p3 = if(d.size <= 4) "" else d(4);
          Some((p1, p2, p3));
        }
      }

    opener match {
      case None => {
        val desktop = Desktop.getDesktop;
        desktop.open(file);
      }
      case Some((p1, p2, p3)) => {
        val f = new File(p2, ".open");
        val fp = new FileOutputStream(f);
        val op = if(p3.isEmpty) new OutputStreamWriter(fp) else
          new OutputStreamWriter(fp, p3);
        try {
          op.write(p1);
        } finally {
          op.close();
        }
      }
    }

  }

}
