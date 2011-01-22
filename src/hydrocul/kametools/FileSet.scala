package hydrocul.kametools;

import java.io.File;
import java.util.NoSuchElementException;

import scala.collection.Iterable;
import scala.collection.IterableLike;
import scala.collection.Iterator;
import scala.collection.immutable.Stream.StreamBuilder;
import scala.collection.mutable.Builder;

trait FileSet extends Iterable[File] with IterableLike[File, FileSet] {

  def name: String;

  def isEmpty: Boolean;

  def head: File;

  def tail: FileSet;

  def isSingleFile: Boolean = {
    try {
      head;
    } catch {
      case _ => return false;
    }
    val t = tail;
    if(!t.isEmpty){
      return false;
    }
    return true;
  }

  def getChild(path: String): FileSet = {
    if(!isSingleFile){
      return FileSet.empty;
    }
    FileSet.OneFileSet(head).getChild(path);
  }

  /**
   * 単一のFileの場合は、そのFileが表すディレクトリの中のリストを
   * 取得する。ディレクトリでない場合または
   * 単一のFileでない場合は、getListと同じ内容を返す。
   */
  def getChildren(reverse: Boolean): FileSet = {
    if(!isSingleFile){
      return this;
    }
    FileSet.DirFileSet(head, reverse);
  }

  override def toString = name;

  override def iterator: Iterator[File] = new Iterator[File](){

    private var fs: FileSet = FileSet.this;

    override def hasNext: Boolean = !fs.isEmpty;

    override def next: File = {
      val ret = fs.head;
      fs = fs.tail;
      ret;
    }

  }

  override protected[this] def newBuilder: Builder[File, FileSet] = {
    (new StreamBuilder).mapResult[FileSet] { stream: Stream[File] =>
      FileSet.StreamFileSet.create(name, stream); }
  }

}

object FileSet {

  case class EmptyFileSet() extends FileSet {

    override def name = "empty";

    override def isEmpty = true;

    override def head = throw new NoSuchElementException(
      "head of empty stream");

    override def tail = throw new UnsupportedOperationException(
      "tail of empty stream");

  }

  val empty = EmptyFileSet();

  case class OneFileSet(file: File) extends FileSet {

    override def name = file.getPath;

    override def isEmpty = false;

    override def head = file;

    override def tail = empty;

    override def getChild(path: String): FileSet = {
      if(path.endsWith("/")){
        DirFileSet((new File(head, path.substring(0, path.length - 1))).
          getCanonicalFile, false);
      } else {
        OneFileSet((new File(head, path)).getCanonicalFile);
      }
    }

    override def getChildren(reverse: Boolean): FileSet = {
      DirFileSet(file, reverse);
    }
    
  }

  case class DirFileSet(file: File, reverse: Boolean) extends FileSet {

    override def name = file.getPath + "/";

    @transient private lazy val files: List[File] = {
      val l = file.listFiles;
      if(l==null){
        Nil;
      } else {
        (if(reverse){
          l.sortWith { (a, b) =>
            compareFileName(a.getName, b.getName) > 0 }
        } else {
          l.sortWith { (a, b) =>
            compareFileName(a.getName, b.getName) < 0 }
        }).toList;
      }
    }

    if(files==null){
      throw new NullPointerException();
    }

    override def isEmpty = {
      if(files==null){
        throw new NullPointerException();
      }
      files.isEmpty;
    }

    override def head = files.head;

    override def tail = ListFileSet.create(name, files.tail);

    override def getChild(path: String): FileSet = {
      OneFileSet(file).getChild(path);
    }

    override def getChildren(reverse: Boolean): FileSet = this;

  }

  case class ListFileSet(override val name: String,
    files: List[File]) extends FileSet {

    override def isEmpty = files.isEmpty;

    override def head = files.head;

    override def tail = ListFileSet.create(name, files.tail);

  }

  object ListFileSet {

    def create(name: String, files: List[File]): FileSet = {
      if(files.isEmpty){
        empty;
      } else if(files.tail.isEmpty){
        OneFileSet(files.head);
      } else {
        ListFileSet(name, files);
      }
    }

  }

  case class StreamFileSet(override val name: String,
    files: Stream[File]) extends FileSet {

    override def isEmpty = files.isEmpty;

    override def head = files.head;

    override def tail = StreamFileSet.create(name, files.tail);

  }

  object StreamFileSet {

    def create(name: String, files: Stream[File]): FileSet = {
      if(files.isEmpty){
        empty;
      } else {
        StreamFileSet(name, files);
      }
    }

  }

  case class ConcatFileSet(override val name: String, headSet: FileSet,
    tailSet: Function0[FileSet]) extends FileSet {

    @transient private lazy val tail2 = tailSet();

    override def isEmpty = {
      if(!headSet.isEmpty){
        false;
      } else {
        tail2.isEmpty;
      }
    }

    override def head: File = {
      if(!headSet.isEmpty){
        headSet.head;
      } else {
        tail2.head;
      }
    }

    override def tail: FileSet = {
      if(!headSet.isEmpty){
        ConcatFileSet(name, headSet.tail, tailSet);
      } else {
        tail2.tail;
      }
    }

  }

  class ParseException(msg: String) extends Exception(msg);

  def getArgFiles(args: Seq[String], ifEmpty: Option[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    env: Env): FileSet = {

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
    env: Env): FileSet = {

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

    // 最初の引数の FileSet を生成
    val firstVD: FileSet = env.objectBank.load("$" + head) match {
      case None =>
        if(!notExistsOk && !file.exists){
          empty;
        } else {
          (list, tail) match {
            case (true, _) => DirFileSet(file, false);
            case (false, None) => OneFileSet(file);
            case (false, Some(tail)) => OneFileSet(file).getChild(tail);
          }
        }
      case Some(ObjectBank.Field(_, f)) =>
        if(file.exists){
          throw new ParseException("duplicated: " + head);
        } else {
          val d: FileSet = f match {
            case f: File => OneFileSet(f.getCanonicalFile);
            case d: FileSet => d;
          }
          (list, tail) match {
            case (true, _) => d.getChildren(false);
            case (false, None) => d;
            case (false, Some(tail)) => d.getChild(tail);
          }
        }
    }

    if(args.size == 1){
      firstVD;
    } else {
      ConcatFileSet(args.mkString(" "), firstVD,
        () => getArgFilesSub(args.tail,
        notExistsOk, enableObjectKey, env));
    }

  }

  def compareFileName(name1: String, name2: String): Int = {
    name1.compareToIgnoreCase(name2);
  }

}

