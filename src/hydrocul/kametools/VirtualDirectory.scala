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

  case class OneFileVirtualDirectory(file: File,
    list: Boolean) extends VirtualDirectory {

    if(file.getCanonicalFile!=file){
      throw new IllegalArgumentException(file.getPath);
    }

    override def getName = file.getPath + (if(list) "/" else "");

    override def getList: Stream[File] = {
      if(!list){
        Stream.cons(file, Stream.empty);
      } else {
        val l = file.listFiles;
        if(l==null){
          Stream.empty;
        } else {
          (l.sortWith { (a, b) => compareFileName(a.getName, b.getName) < 0 }).
            toStream;
        }
      }
    }

    override def getChild(path: String): VirtualDirectory = {
      if(path.endsWith("/")){
        OneFileVirtualDirectory((new File(file,
          path.substring(0, path.length - 1))).getCanonicalFile, true);
      } else {
        OneFileVirtualDirectory((new File(file,
          path)).getCanonicalFile, false);
      }
    }

    override def getChildren: VirtualDirectory = {
      if(list){
        this;
      } else {
        OneFileVirtualDirectory(file, true);
      }
    }

  }

  case class ConcatVirtualDirectory(name: String, head: VirtualDirectory,
    tail: Function0[VirtualDirectory]) extends VirtualDirectory {

    override def getName = name;

    override def getList: Stream[File] = {
      val l = head.getList;
      Stream.cons(l.head, {
        if(!l.tail.isEmpty){
          l.tail;
        } else {
          tail.apply().getList;
        }
      });
    }

    override def getChild(path: String) = empty;

    override def getChildren = this;

  }

  case class ReverseVirtualDirectory(src: VirtualDirectory) extends VirtualDirectory {

    override def getName = src.getName;

    override def getList: Stream[File] = src.getList.reverse;

    override def getChild(path: String) = src.getChild(path);

    override def getChildren = ReverseVirtualDirectory(src.getChildren);

  }

  case class EmptyVirtualDirectory() extends VirtualDirectory {

    override def getName = "empty";

    override def getList = Stream.empty;

    override def getChild(path: String) = this;

    override def getChildren = this;

  }

  val empty = EmptyVirtualDirectory();

  class ParseException(msg: String) extends Exception(msg);

  def getArgFiles(args: Seq[String], ifEmpty: Option[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    env: Env): VirtualDirectory = {
    getArgFiles(args, ifEmpty, notExistsOk, enableObjectKey, false, env);
  }

  def getArgFiles(args: Seq[String], ifEmpty: Option[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    reverseOrder: Boolean, env: Env): VirtualDirectory = {

    if(args.size == 0){
      // 引数がない場合
      ifEmpty match {
        case None => empty;
        case Some(p) => getArgFiles(Array(p), None,
          notExistsOk, enableObjectKey, reverseOrder, env);
      }
    } else {
      // 引数がある場合
      getArgFilesSub(args, notExistsOk, enableObjectKey, reverseOrder, env);

    }
  }

  private def getArgFilesSub(args: Seq[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    reverseOrder: Boolean, env: Env): VirtualDirectory = {

    val a = args.head;
    val htl = {
      val i = a.indexOf('/');
      if(i < 0){
        (a, None, false);
      } else if(i == a.length - 1){
        (a.substring(0, i), None, true);
      } else {
        (a.substring(0, i), Some(a.substring(i + 1)), false);
      }
    }
    val head: String = htl._1;
    val tail: Option[String] = htl._2;
    val list: Boolean = htl._3;

    val path = if(head.length == 0) "/" else head;
    val file = (new File(path)).getCanonicalFile;

    // 最初の引数の VirtualDirectory を生成
    val firstVD: VirtualDirectory = env.objectBank.load("$" + head) match {
      case None =>
        if(!notExistsOk && !file.exists){
          empty;
        } else {
          (list, tail) match {
            case (true, _) => OneFileVirtualDirectory(file, true);
            case (false, None) => OneFileVirtualDirectory(file, false);
            case (false, Some(tail)) => OneFileVirtualDirectory(file, false).
              getChild(tail);
          }
        }
      case Some(ObjectBank.Field(_, f)) =>
        if(file.exists){
          throw new ParseException("duplicated: " + head);
        } else {
          val d: VirtualDirectory = f match {
            case f: File => OneFileVirtualDirectory(f.getCanonicalFile, false);
            case d: VirtualDirectory => d;
          }
          (list, tail) match {
            case (true, _) => d.getChildren;
            case (false, None) => d;
            case (false, Some(tail)) => d.getChild(tail);
          }
        }
    }

    val firstVD2 = if(reverseOrder){
      ReverseVirtualDirectory(firstVD);
    } else {
      firstVD;
    }

    if(args.size == 1){
      firstVD2;
    } else {
      ConcatVirtualDirectory(args.mkString(" "), firstVD2,
        () => getArgFilesSub(args.tail,
        notExistsOk, enableObjectKey, reverseOrder, env));
    }

  }

  def compareFileName(name1: String, name2: String): Int = {
    name1.compareToIgnoreCase(name2);
  }

}
