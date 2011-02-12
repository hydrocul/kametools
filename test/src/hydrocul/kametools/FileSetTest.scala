package hydrocul.kametools;

import java.io.File;

import hydrocul.kametest.Test;

object FileSetTest {

  def test(){

    test1();
    test2();

  }

  private def test1(){

    val srcFile = new File("test").getAbsoluteFile;
    val srcFileSet = FileSet.OneFileSet(srcFile);

    Test.assertEquals("", true, srcFileSet.isSingleFile);
    Test.assertEquals("", srcFile, srcFileSet.head);
    Test.assertEquals("", 1, srcFileSet.size);

    val childrenFileSet = srcFileSet.getChildren(false);

    Test.assertEquals("", false, childrenFileSet.isSingleFile);
    Test.assertEquals("", new File(srcFile, "class"), childrenFileSet.head);
    Test.assertEquals("", new File(srcFile, "src"), childrenFileSet.tail.head);
    Test.assertEquals("", 3, childrenFileSet.size);

    val concatFileSet = FileSet.ConcatFileSet(srcFileSet,
      ()=>srcFileSet.getChildren(true));

    Test.assertEquals("", false, concatFileSet.isSingleFile);
    Test.assertEquals("", srcFile, concatFileSet.head);
    Test.assertEquals("", new File(srcFile, "touch"), concatFileSet.tail.head);
    Test.assertEquals("", 4, concatFileSet.size);

  }

  private def test2(){

/*
    val fs = FileSet.getArgFiles(Array[String](), Some("./"), false, true);
    Test.assertEquals("", false, fs.isSingleFile);
*/

  }

}
