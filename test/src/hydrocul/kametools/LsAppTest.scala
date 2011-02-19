package hydrocul.kametools;

import java.io.File;

import hydrocul.kametest.Test;

object LsAppTest {

  def test(){
    test1();
    test2();
    test3();
    test4();
    test5();
    test6();
    test7();
    test8();
  }

  private def test1(){

    val env = new App.StringEnv();

    val file = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file);
    val app = LsApp(fileSet);

    app.exec(env);

    Test.assertEquals("", Test.StringPattern(
      ("20[0-9][0-9]-[01][0-9]-[0-3][0-9]-[0-2][0-9]-[0-5][0-9]-[0-5][0-9]" +
      " %s  \\[[a-z]+\\]\n").format(file.getPath)), env.getOutput());

  }

  private def test2(){

    val env = new App.StringEnv();

    val file = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file);
    val app = LsApp(fileSet).next("-t").next("%Y");

    app.exec(env);

    Test.assertEquals("", Test.StringPattern(
      "20[0-9][0-9] %s  \\[[a-z]+\\]\n".format(file.getPath)), env.getOutput());

  }

  private def test3(){

    val env = new App.StringEnv();

    val file = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file);
    val app = LsApp(fileSet).next("-f").next("-- %2 --");

    app.exec(env);

    Test.assertEquals("", Test.StringPattern(
      "-- %s --\n".format(file.getPath)), env.getOutput());

  }

  private def test4(){

    val env = new App.StringEnv();

    val file1 = new File("src").getAbsoluteFile;
    val file2 = new File("test").getAbsoluteFile;
    val fileSet = FileSet(file1 :: file2 :: Nil);
    val app = LsApp(fileSet).next("-f").next("%4").next("-a");

    app.exec(env);

    Test.assertEquals("", "src\ntest\n", env.getOutput());

  }

  private def test5(){

    val env = new App.StringEnv();

    val file1 = new File("src").getAbsoluteFile;
    val file2 = new File("test").getAbsoluteFile;
    val fileSet = FileSet(file1 :: file2 :: Nil);
    val app = LsApp(fileSet).next("-f").next("%4").next("-c1");

    app.exec(env);

    Test.assertEquals("", Test.StringPattern(
      "src\nshow more: \\[[a-z]+\\]\n"), env.getOutput());

  }

  private def test6(){

    val env = new App.StringEnv();

    val file1 = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file1);
    val app = LsApp(fileSet).next("-r1").next("-f").next("%4");

    app.exec(env);

    Test.assertEquals("", Test.StringPattern(
      "src\n" +
      "hydrocul\n"), env.getOutput());

  }

  private def test7(){

    val env = new App.StringEnv();

    val file1 = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file1);
    val app = LsApp(fileSet).next("-r1-2").next("-f").next("%4");

    app.exec(env);

    Test.assertEquals("", Test.StringPattern(
      "hydrocul\n" +
      "kametools\n"), env.getOutput());

  }

  private def test8(){

    val env = new App.StringEnv();

    val file1 = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file1);
    val app = LsApp(fileSet).next("-r1").next("-v").next("-f").next("%4");

    app.exec(env);

    Test.assertEquals("", Test.StringPattern(
      "hydrocul\n" +
      "src\n"), env.getOutput());

  }

}
