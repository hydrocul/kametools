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

    app.main(Array("-T", "%Y"), env);

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

}
