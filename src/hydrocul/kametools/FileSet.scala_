package hydrocul.kametools;

import java.io.File;
import java.util.NoSuchElementException;

import scala.collection.Iterable;
import scala.collection.IterableLike;
import scala.collection.Iterator;
import scala.collection.immutable.Stream.StreamBuilder;
import scala.collection.mutable.Builder;
import scala.collection.mutable.LazyBuilder

trait FileSet extends Iterable[File] with IterableLike[File, FileSet] {

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

  override def iterator: Iterator[File] = new FileSetIterator();

  private case class FileSetIterator() extends Iterator[File] {

    private var fs: FileSet = FileSet.this;

    override def hasNext: Boolean = !fs.isEmpty;

    override def next: File = {
      val ret = fs.head;
      fs = fs.tail;
      ret;
    }

  }

  override protected[this] def newBuilder: Builder[File, FileSet] = {
    new LazyBuilder[File, FileSet]{
      override def result: FileSet = {
        FileSet(parts.toIterable.flatMap(_.toIterable));
      }
    }
  }

}

object FileSet {

  def apply(file: File): FileSet = {
    OneFileSet(file);
  }

  def apply(files: List[File]): FileSet = {
    if(files.isEmpty){
      empty;
    } else if(files.tail.isEmpty){
      OneFileSet(files.head);
    } else {
      ListFileSet(files);
    }
  }

  def apply(files: Iterable[File]): FileSet = {
    if(files.isEmpty){
      empty;
    } else {
      IterableFileSet(files);
    }
  }

  def recursive(srcFileSet: FileSet, depth: Int, reverse: Boolean): FileSet = {
    LsFileSet(srcFileSet, depth, reverse, empty);
  }

  def concat(fileSet1: FileSet, fileSet2: FileSet): FileSet = {
    ConcatFileSet(fileSet1, fileSet2);
  }

  val empty: FileSet = EmptyFileSet();

  private case class EmptyFileSet() extends FileSet {

    override def isEmpty = true;

    override def head = throw new NoSuchElementException(
      "head of empty stream");

    override def tail = throw new UnsupportedOperationException(
      "tail of empty stream");

  }

  case class OneFileSet(file: File) extends FileSet {

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

    @transient private var _files: List[File] = null;
    private def files: List[File] = {
      if(_files==null){
        synchronized {
          if(_files==null){
            _files = $files;
          }
        }
      }
      _files;
    }
    private def $files: List[File] = {
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

    override def tail: FileSet = FileSet(files.tail);

    override def getChild(path: String): FileSet = {
      OneFileSet(file).getChild(path);
    }

    override def getChildren(reverse: Boolean): FileSet = this;

  }

  private case class ListFileSet(files: List[File]) extends FileSet {

    override def isEmpty = files.isEmpty;

    override def head = files.head;

    override def tail: FileSet = FileSet(files.tail);

  }

  private case class IterableFileSet(files: Iterable[File]) extends FileSet {

    override def isEmpty = files.isEmpty;

    override def head = files.head;

    override def tail: FileSet = FileSet(files.tail);

  }

  private case class ConcatFileSet(headSet: FileSet,
    tailSet: FileSet) extends FileSet {

    override def isEmpty = {
      if(!headSet.isEmpty){
        false;
      } else {
        tailSet.isEmpty;
      }
    }

    override def head: File = {
      if(!headSet.isEmpty){
        headSet.head;
      } else {
        tailSet.head;
      }
    }

    override def tail: FileSet = {
      if(headSet.isEmpty){
        tailSet.tail;
      } else if(headSet.tail.isEmpty){
        tailSet;
      } else {
        ConcatFileSet(headSet.tail, tailSet);
      }
    }

  }

  private case class LsFileSet(files: FileSet, depth: Int, reverseOrder: Boolean,
    next: FileSet) extends FileSet {

    override def isEmpty: Boolean = if(!files.isEmpty){
      false;
    } else {
      next.isEmpty;
    }

    @transient private var _headTail: (File, FileSet) = null;
    private def headTail: (File, FileSet) = {
      if(_headTail==null){
        synchronized {
          if(_headTail==null){
            _headTail = $headTail;
          }
        }
      }
      _headTail;
    }
    private def $headTail: (File, FileSet) = {
      if(files.isEmpty){
        (next.head, next.tail);
      } else {
        val f = files.head;
        val tail = if(depth==0){
          LsFileSet(files.tail, depth, reverseOrder, next);
        } else {
          LsFileSet(FileSet.DirFileSet(f, reverseOrder),
            depth - 1, reverseOrder, LsFileSet(files.tail, depth,
            reverseOrder, next));
        }
        (f, tail);
      }
    }

    override def head: File = headTail._1;

    override def tail: FileSet = headTail._2;

  }

  def getArgFiles(args: Seq[String], ifEmpty: Option[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    reverse: Boolean): FileSet = {

    if(args.size == 0){
      // 引数がない場合
      ifEmpty match {
        case None => empty;
        case Some(p) => getArgFilesSub(Array(p),
          notExistsOk, enableObjectKey, reverse);
      }
    } else {
      // 引数がある場合
      getArgFilesSub(args, notExistsOk, enableObjectKey, reverse);
    }
  }

  private def getArgFilesSub(args: Seq[String],
    notExistsOk: Boolean, enableObjectKey: Boolean,
    reverse: Boolean): FileSet = {

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
    val firstVD: FileSet = ObjectBank.default.get(head) match {
      case None =>
        if(!notExistsOk && !file.exists){
          empty;
        } else {
          (list, tail) match {
            case (true, _) => DirFileSet(file, reverse);
            case (false, None) => OneFileSet(file);
            case (false, Some(tail)) => OneFileSet(file).getChild(tail);
          }
        }
      case Some(f: FileSet) =>
        if(file.exists){
          throw new Exception("duplicated: " + head);
        } else {
          (list, tail) match {
            case (true, _) => f.getChildren(reverse);
            case (false, None) => f;
            case (false, Some(tail)) => f.getChild(tail);
          }
        }
      case Some(f: File) =>
        if(file.exists){
          throw new Exception("duplicated: " + head);
        } else {
          val d: FileSet = OneFileSet(f.getCanonicalFile);
          (list, tail) match {
            case (true, _) => d.getChildren(reverse);
            case (false, None) => d;
            case (false, Some(tail)) => d.getChild(tail);
          }
        }
    }

    if(args.size == 1){
      firstVD;
    } else {
      ConcatFileSet(firstVD, getArgFilesSub(args.tail,
        notExistsOk, enableObjectKey, reverse));
    }

  }

  def compareFileName(name1: String, name2: String): Int = {
    name1.compareToIgnoreCase(name2);
  }

}

