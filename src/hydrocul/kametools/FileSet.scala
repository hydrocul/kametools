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

  def reverse: FileSet;

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
  def getChildren(reverseFlag: Boolean): FileSet = {
    if(!isSingleFile){
      return this;
    }
    FileSet.DirFileSet(head, reverseFlag);
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

  def recursive(srcFileSet: FileSet, depthStart: Int, depthEnd: Int,
    reverseFlag: Boolean): FileSet = {
    if(depthStart <= 0 && depthEnd == 0){
      srcFileSet;
    } else {
      LsFileSet(srcFileSet, depthStart, depthEnd, reverseFlag);
    }
  }

  def concat(fileSet1: FileSet, fileSet2: FileSet): FileSet = {
    if(fileSet1.isEmpty){
      fileSet2;
    } else if(fileSet2.isEmpty){
      fileSet1;
    } else {
      ConcatFileSet(fileSet1, fileSet2);
    }
  }

  val empty: FileSet = EmptyFileSet();

  private case class EmptyFileSet() extends FileSet {

    override def isEmpty = true;

    override def head = throw new NoSuchElementException(
      "head of empty stream");

    override def tail = throw new UnsupportedOperationException(
      "tail of empty stream");

    override def reverse = this;

  }

  case class OneFileSet(file: File) extends FileSet {

    override def isEmpty = false;

    override def head = file;

    override def tail = empty;

    override def reverse = this;

    override def getChild(path: String): FileSet = {
      if(path.endsWith("/")){
        DirFileSet((new File(head, path.substring(0, path.length - 1))).
          getCanonicalFile, false);
      } else {
        OneFileSet((new File(head, path)).getCanonicalFile);
      }
    }

    override def getChildren(reverseFlag: Boolean): FileSet = {
      DirFileSet(file, reverseFlag);
    }
    
  }

  case class DirFileSet(file: File, reverseFlag: Boolean) extends FileSet {

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
        (if(reverseFlag){
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

    override def reverse = DirFileSet(file, !reverseFlag);

    override def getChild(path: String): FileSet = {
      OneFileSet(file).getChild(path);
    }

    override def getChildren(reverseFlag: Boolean): FileSet = this;

  }

  private case class ListFileSet(files: List[File]) extends FileSet {

    override def isEmpty = files.isEmpty;

    override def head = files.head;

    override def tail: FileSet = FileSet(files.tail);

    override def reverse = ListFileSet(files.reverse);

  }

  private case class IterableFileSet(files: Iterable[File]) extends FileSet {

    override def isEmpty = files.isEmpty;

    override def head = files.head;

    override def tail: FileSet = FileSet(files.tail);

    override def reverse: FileSet = ListFileSet(files.toList.reverse);

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

    override def reverse = ConcatFileSet(tailSet.reverse, headSet.reverse);

  }

  private case class LsFileSet(files: FileSet, depthStart: Int, depthEnd: Int,
    reverseFlag: Boolean) extends FileSet {

    override def isEmpty: Boolean = !headTail.isDefined;

    @transient private var _headTail: Option[(File, FileSet)] = null;
    private def headTail: Option[(File, FileSet)] = {
      if(_headTail==null){
        synchronized {
          if(_headTail==null){
            _headTail = $headTail;
          }
        }
      }
      _headTail;
    }
    private def $headTail: Option[(File, FileSet)] = {
      if(files.isEmpty){
        None;
      } else {
        val fs = if(depthEnd==0){
          files;
        } else {
          val f = files.head;
          if(depthStart > 0){
            concat(recursive(DirFileSet(f, reverseFlag),
              depthStart - 1, depthEnd - 1, reverseFlag),
              recursive(files.tail, depthStart, depthEnd, reverseFlag));
          } else if(reverseFlag){
            concat(concat(recursive(DirFileSet(f, reverseFlag),
              depthStart - 1, depthEnd - 1, reverseFlag),
              OneFileSet(f)),
              recursive(files.tail, depthStart, depthEnd, reverseFlag));
          } else {
            ConcatFileSet(OneFileSet(f),
              ConcatFileSet(recursive(DirFileSet(f, reverseFlag),
              depthStart - 1, depthEnd - 1, reverseFlag),
              recursive(files.tail, depthStart, depthEnd, reverseFlag)));
          }
        }
        if(fs.isEmpty){
          None;
        } else {
          Some((fs.head, fs.tail));
        }
      }
    }

    override def head: File = headTail match {
      case Some((h, _)) => h;
      case None => empty.head;
    }

    override def tail: FileSet = headTail match {
      case Some((_, t)) => t;
      case None => empty.tail;
    }

    override def reverse = LsFileSet(files.reverse, depthStart, depthEnd,
      !reverseFlag);

  }

  def compareFileName(name1: String, name2: String): Int = {
    name1.compareToIgnoreCase(name2);
  }

}

