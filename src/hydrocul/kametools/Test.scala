package hydrocul.kametools;

import java.io.File;

object Test {

  def main(args: Array[String]){

    val ob = new ObjectBank(getDirName());

    val vd = FileSet.DirFileSet(new File("/home/kenken/projects"), false);
    vd.head;
    ob.put("test", "test", vd);

    println(ob.load("test").get.get);

    

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
            FileSet.compareFileName(a.getName, b.getName) > 0 }
        } else {
          l.sortWith { (a, b) =>
            FileSet.compareFileName(a.getName, b.getName) < 0 }
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

    override def tail = FileSet.ListFileSet.create(name, files.tail);

    override def getChild(path: String): FileSet = {
      FileSet.OneFileSet(file).getChild(path);
    }

    override def getChildren(reverse: Boolean): FileSet = this;

  }

  case class ClassA(name: String){

    @transient private lazy val msg = "Hello, " + name;

    override def toString = msg;

  }

  private def getDirName(): String = {
    import java.io.File;
    System.getProperty("user.home") + File.separator + ".kametools";
  }

}
