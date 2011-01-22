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

    override def isEmpty = files.isEmpty;

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

    @transient lazy val tail2 = tailSet();

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

  def compareFileName(name1: String, name2: String): Int = {
    name1.compareToIgnoreCase(name2);
  }

}

