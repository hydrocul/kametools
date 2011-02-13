package hydrocul.kametools.ls;

import java.io.File;

import hydrocul.kametest.Test;
import hydrocul.kametools.App;
import hydrocul.kametools.FileSet;

object LsAppTest {

  def test(){
    test1();
    test2();
    test3();
    test4();
    test5();
  }

  private def test1(){

    val env = new App.StringEnv();

    val file = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file);
    val app = LsApp(fileSet);

    app.main(Array[String](), env);

    Test.assertEquals("", Test.StringPattern(
      ("20[0-9][0-9]-[01][0-9]-[0-3][0-9]-[0-2][0-9]-[0-5][0-9]-[0-5][0-9]" +
      " %s  \\[[a-z]+\\]\n").format(file.getPath)), env.getOutput());

  }

  private def test2(){

    val env = new App.StringEnv();

    val file = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file);
    val app = LsApp(fileSet);

    app.main(Array("-t", "%Y"), env);

    Test.assertEquals("", Test.StringPattern(
      "20[0-9][0-9] %s  \\[[a-z]+\\]\n".format(file.getPath)), env.getOutput());

  }

  private def test3(){

    val env = new App.StringEnv();

    val file = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file);
    val app = LsApp(fileSet);

    app.main(Array("-f", "-- %2 --"), env);

    Test.assertEquals("", Test.StringPattern(
      "-- %s --\n".format(file.getPath)), env.getOutput());

  }

  private def test4(){

    val env = new App.StringEnv();

    val file1 = new File("src").getAbsoluteFile;
    val file2 = new File("test").getAbsoluteFile;
    val fileSet = FileSet(file1 :: file2 :: Nil);
    val app = LsApp(fileSet);

    app.main(Array("-f", "%4", "-a"), env);

    Test.assertEquals("", "src\ntest\n", env.getOutput());

  }

  private def test5(){

    val env = new App.StringEnv();

    val file1 = new File("src").getAbsoluteFile;
    val file2 = new File("test").getAbsoluteFile;
    val fileSet = FileSet(file1 :: file2 :: Nil);
    val app = LsApp(fileSet);

    app.main(Array("-f", "%4", "-c", "1"), env);

    Test.assertEquals("", Test.StringPattern(
      "src\nshow more: \\[[a-z]+\\]\n"), env.getOutput());

  }

}
