package hydrocul.kametools;

import java.io.File;

trait App {

  def main(cmdName: String, args: Array[String], env: App.Env);

  def help(cmdName: String);

}

object App {

  class Env(val objectBank: ObjectBank);

  def getArgFiles(args: Seq[String], currentDirIfEmpty: Boolean,
    extractDir: Boolean, notExistsOk: Boolean,
    enableObjectKey: Boolean, env: Env): Seq[File] = {

    def compare(name1: String, name2: String): Int = {
      name1.compareToIgnoreCase(name2);
    }

    if(currentDirIfEmpty && args.size==0){
      // 引数がない場合
      getArgFiles(Array("./"), false, extractDir, notExistsOk, false, env);
    } else {
      args.flatMap { a =>
        val f = new File(a);
        if(f.exists || notExistsOk){
          if(extractDir && (a.endsWith("/") || a.endsWith("\\"))){
            // 引数がスラッシュで終わっている場合は
            // ディレクトリの中を表示する
            if(f.exists){
              val l = f.listFiles;
              if(l==null){
                Nil;
              } else {
                l;
              }
            } else {
              Nil;
            }
          } else {
            f :: Nil;
          }
        } else if(enableObjectKey){
          val (head, tail) = {
            val i = a.indexOf('/');
            if(i < 0){
              (a, "");
            } else {
              (a.substring(0, i), a.substring(i));
            }
          }
          val k = (env.objectBank.load("$" + head) match {
            case None => Nil;
            case Some(ObjectBank.Field(_, f)) => f match {
              case f: File => f.getCanonicalPath :: Nil;
              case _ => Nil;
            }
          }).map(p => p + tail).toArray;
          getArgFiles(k, false, extractDir, notExistsOk, false, env);
        } else {
          Nil;
        }
      }
    }.filter(_.exists).map(_.getCanonicalFile).
      sortWith { (a, b) => compare(a.getName, b.getName) < 0 }

  }

}
