package hydrocul.kametools;

import java.io.File;

trait VirtualDirectory {

  def getName: String;

  def getList: Stream[File];

  def getChild(path: String): VirtualDirectory;

  /**
   * 単一のFileの場合は、そのFileが表すディレクトリの中のリストを
   * 取得する。ディレクトリでない場合は、emptyを返す。
   * 単一のFileでない場合は、getListと同じ内容を返す。
   */
  def getChildren: VirtualDirectory;

  override def toString = getName;

}

object VirtualDirectory {

  def fromFile(file: File, list: Boolean): VirtualDirectory = {
    val f = file.getCanonicalFile;
    new VirtualDirectory {
      override def getName = f.getPath + (if(list) "/" else "");
      override def getList: Stream[File] = if(!list){
        Stream.cons(f, Stream.empty);
      } else {
        val l = f.listFiles;
        if(l==null){
          Stream.empty;
        } else {
          (l.sortWith { (a, b) => compareFileName(a.getName, b.getName) < 0 }).
            toStream;
        }
      }
      override def getChild(path: String) = {
        if(path.endsWith("/")){
          fromFile(new File(f, path.substring(0, path.length - 1)), true);
        } else {
          fromFile(new File(f, path), false);
        }
      }
      override def getChildren: VirtualDirectory = if(list){
        this;
      } else {
        fromFile(f, true);
      }
    }
  }

  def concat(name: String, head: VirtualDirectory,
    tail: => VirtualDirectory): VirtualDirectory = {
    new VirtualDirectory {
      override def getName = name;
      override def getList: Stream[File] = {
        val l = head.getList;
        Stream.cons(l.head, {
          if(!l.tail.isEmpty){
            l.tail;
          } else {
            tail.getList;
          }
        });
      }
      override def getChild(path: String) = empty;
      override def getChildren = this;
    }
  }

  val empty: VirtualDirectory = {
    new VirtualDirectory {
      override def getName = "empty";
      override def getList = Stream.empty;
      override def getChild(path: String) = this;
      override def getChildren = this;
    }
  }

  class ParseException(msg: String) extends Exception(msg);

  def getArgFiles(args: Seq[String], ifEmpty: Option[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    env: Env): VirtualDirectory = {

    if(args.size == 0){
      // 引数がない場合
      ifEmpty match {
        case None => empty;
        case Some(p) => getArgFiles(Array(p), None,
          notExistsOk, enableObjectKey, env);
      }
    } else {
      // 引数がある場合
      getArgFilesSub(args, notExistsOk, enableObjectKey, env);

    }
  }

  private def getArgFilesSub(args: Seq[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    env: Env): VirtualDirectory = {

    val a = args.head;
    val htl = {
      val i = a.indexOf('/');
      if(i < 0){
        (a, None, false);
      } else if(i == a.length - 1){
        (a.substring(0, i), None, true);
      } else {
        (a.substring(0, i), Some(a.substring(i)), false);
      }
    }
    val head: String = htl._1;
    val tail: Option[String] = htl._2;
    val list: Boolean = htl._3;

    val file = new File(head);

    // 最初の引数の VirtualDirectory を生成
    val firstVD: VirtualDirectory = env.objectBank.load("$" + head) match {
      case None =>
        if(!notExistsOk && !file.exists){
          empty;
        } else {
          (list, tail) match {
            case (true, _) => fromFile(file, true);
            case (false, None) => fromFile(file, false);
            case (false, Some(tail)) => fromFile(file, false).getChild(tail);
          }
        }
      case Some(ObjectBank.Field(_, f)) =>
        if((new File(head)).exists){
          throw new ParseException("duplicated: " + head);
        } else {
          val d: VirtualDirectory = f match {
            case f: File => fromFile(f, false);
            case d: VirtualDirectory => d;
          }
          (list, tail) match {
            case (true, _) => d.getChildren;
            case (false, None) => d;
            case (false, Some(tail)) => d.getChild(tail);
          }
        }
    }

    if(args.size == 1){
      firstVD;
    } else {
      concat(args.mkString(" "), firstVD, getArgFilesSub(args.tail,
        notExistsOk, enableObjectKey, env));
    }

  }

  def compareFileName(name1: String, name2: String): Int = {
    name1.compareToIgnoreCase(name2);
  }

}
