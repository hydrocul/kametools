package hydrocul.kametools;

import java.io.File;

import java.util.NoSuchElementException;

trait VirtualDirectory {

  def getName: String;

  def isEmpty: Boolean;

  /**
   * 含まれるファイルリストの1つ目と残りを表す VirtualDirectory を取得する。
   * ない場合は、NoSuchElementException をスローする。
   */
  def getList: (File, VirtualDirectory);

  def getChild(path: String): VirtualDirectory;

  /**
   * 単一のFileの場合は、そのFileが表すディレクトリの中のリストを
   * 取得する。ディレクトリでない場合または
   * 単一のFileでない場合は、getListと同じ内容を返す。
   */
  def getChildren: VirtualDirectory = {
    val list = getList;
    val head = try {
      list.head;
    } catch {
      case e: NoSuchElementException =>
        return VirtualDirectory.empty;
    }
    if(!list.tail.isEmpty)
      return this;
    VirtualDirectory.OneFileVirtualDirectory(head, true);
  }

  private val src = this;

  def getReverse: VirtualDirectory = new VirtualDirectory(){

    override def getName = src.getName;

    override def isEmpty = src.isEmpty;

    override def getList: (File, VirtualDirectory) = {
      val list = src.getList.toList;
      ListVirtualDirectory(src.getName, list).getReverse.getList;
    }

    override def getChild(path: String) = src.getChild(path);

  }

  override def toString = getName;

}

object VirtualDirectory {

  case class OneFileVirtualDirectory(file: File,
    list: Boolean, reverse: Boolean) extends VirtualDirectory {

    if(file.getCanonicalFile!=file){
      throw new IllegalArgumentException(file.getPath);
    }

    override def getName = file.getPath + (if(list) "/" else "");

    override def isEmpty = false;

    override def getList: (File, VirtualDirectory) = {
      if(!list){
        (file, empty);
      } else {
        val l = file.listFiles;
        if(l==null){
          (file, empty);
        } else {
          FilesVirtualDirectory.create(l.sortWith {
            (a, b) => compareFileName(a.getName, b.getName) < 0 },
            reverse).getList;
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

    override def getReverse: VirtualDirectory = OneFileVirtualDirectory(file,
      list, !reverse);

  }

  case class ConcatVirtualDirectory(name: String, head: VirtualDirectory,
    tail: Function0[VirtualDirectory]) extends VirtualDirectory {

    @transient lazy val tail2 = tail();

    override def getName = name;

    override def isEmpty: Boolean = {
      if(!head.isEmpty){
        false;
      } else {
        tail2.isEmpty;
      }
    }

    override def getList: (File, VirtualDirectory) = {
      if(head.isEmpty){
        tail2.getList;
      } else {
        val r = head.getList;
        (r._1, ConcatVirtualDirectory.create(name, r._2, tail2));
      }
    }

    override def getChild(path: String) = empty;

  }

  object ConcatVirtualDirectory {

    def create(name: String, head: VirtualDirectory,
      tail: Function0[VirtualDirectory]): VirtualDirectory = {
      if(head.isEmpty){
        tail();
      } else {
        ConcatVirtualDirectory(name, head, tail);
      }
    }

  }

  case class ListVirtualDirectory(name: String,
    files: List[File]) extends VirtualDirectory {

    override def getName = name;

    override def isEmpty = files.isEmpty;

    override def getList = (files.head, ListVirtualDirectory(name, files.tail));

    override def getReverse = ListVirtualDirectory(name, files.reverse);

  }

  case class EmptyVirtualDirectory() extends VirtualDirectory {

    override def getName = "empty";

    override def getList = Stream.empty;

    override def getChild(path: String) = this;

    override def getChildren = this;

    override def getReverse = this;

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
