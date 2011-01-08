package hydrocul.kametools.ls;

import java.io.File;

import scala.annotation.tailrec;

import hydrocul.util.StringLib;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object Ls extends App {

  def main(cmdName: String, args: Array[String], env: App.Env){

    val list: Seq[File] = if(args.size==0){
      // 引数がない場合
      val currFile = new File(System.getProperty("user.dir"));
      val list = currFile.listFiles;
      if(list==null){
        throw new Exception();
      }
      list;
    } else {
      args.flatMap { a =>
        if(a.endsWith("/") || a.endsWith("\\")){
          // 引数がスラッシュで終わっている場合は
          // ディレクトリの中を表示する
          val f = new File(a);
          val l = f.listFiles;
          if(l==null){
            f :: Nil;
          } else {
            l;
          }
        } else {
          // 引数で指定されたファイルを表示する
          val f = new File(a);
          if(!f.exists){
            env.objectBank.load("$" + a) match {
              case None => Nil;
              case Some(ObjectBank.Field(_, f2)) => f2 match {
                case f3: File => f3 :: Nil;
                case _ => Nil;
              }
            }
          } else {
            f :: Nil;
          }
        }
      }
    }.filter(_.exists).map(_.getCanonicalFile).
      sortWith { (a, b) => compare(a.getName, b.getName) < 0 }

    var map = env.objectBank.getOrElse[Map[File, String]]("$files", Map());

    list.foreach { f: File =>
      @tailrec
      def createName(): String = {
        val r = StringLib.createRandomString(3);
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

  private def compare(name1: String, name2: String): Int = {
    name1.compareToIgnoreCase(name2);
  }

  def help(cmdName: String){
    // TODO
  }

}
