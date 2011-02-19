package hydrocul.kametools;

import java.io.File;

import hydrocul.kametest.Test;

object FileSetTest {

  def test(){

    test1();
    test2();
    test3();

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

    val concatFileSet = FileSet.concat(srcFileSet,
      srcFileSet.getChildren(true));

    Test.assertEquals("", false, concatFileSet.isSingleFile);
    Test.assertEquals("", srcFile, concatFileSet.head);
    Test.assertEquals("", new File(srcFile, "touch"), concatFileSet.tail.head);
    Test.assertEquals("", 4, concatFileSet.size);

  }

  private def test2(){

    val srcFile1 = new File("src").getAbsoluteFile;
    val srcFile2 = new File("test").getAbsoluteFile;

    val fileSet1 = FileSet(srcFile1 :: srcFile2 :: Nil);

    Test.assertEquals("", false, fileSet1.isSingleFile);
    Test.assertEquals("", srcFile1, fileSet1.head);
    Test.assertEquals("", srcFile2, fileSet1.tail.head);
    Test.assertEquals("", 2, fileSet1.size);

  }

  private def test3(){

    val srcFile1 = new File("src").getAbsoluteFile;
    val srcFile2 = new File("test").getAbsoluteFile;

    val fileSet2 = FileSet(srcFile1 :: srcFile2 :: Nil);

    {
      val result = FileSet.recursive(fileSet2, 0, 0, false).
        map(_.getName).mkString(" ");
      Test.assertEquals("", "src test", result);
    }

    {
      val result = FileSet.recursive(fileSet2, 0, 1, false).
        map(_.getName).mkString(" ");
      Test.assertEquals("", "src hydrocul test class src touch", result);
    }

    {
      val result = FileSet.recursive(fileSet2, 0, 1, true).
        map(_.getName).mkString(" ");
      Test.assertEquals("", "src hydrocul test touch src class", result);
    }

    {
      val result = FileSet.recursive(fileSet2, 1, 1, true).
        map(_.getName).mkString(" ");
      Test.assertEquals("", "hydrocul touch src class", result);
    }

  }

}
