package hydrocul.kametools;

import java.io.File;

trait App {

  def main(cmdName: String, args: Array[String], env: App.Env);

  def help(cmdName: String);

}

object App {

  class Env(val objectBank: ObjectBank);

  def getArgFiles(args: Seq[String], currentDirIfEmpty: Boolean,
    extractDir: Boolean, env: Env): Seq[File] = {

    def compare(name1: String, name2: String): Int = {
      name1.compareToIgnoreCase(name2);
    }

    if(currentDirIfEmpty && args.size==0){
      // 引数がない場合
      val currFile = new File(System.getProperty("user.dir"));
      if(extractDir){
        val list = currFile.listFiles;
        if(list==null){
          throw new Exception();
        }
        list;
      } else {
        currFile :: Nil;
      }
    } else {
      args.flatMap { a =>
        if(extractDir && (a.endsWith("/") || a.endsWith("\\"))){
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
          if(f.exists){
            f :: Nil;
          } else {
            env.objectBank.load("$" + a) match {
              case None => Nil;
              case Some(ObjectBank.Field(_, f2)) => f2 match {
                case f3: File => f3 :: Nil;
                case _ => Nil;
              }
            }
          }
        }
      }
    }.filter(_.exists).map(_.getCanonicalFile).
      sortWith { (a, b) => compare(a.getName, b.getName) < 0 }

  }

}
