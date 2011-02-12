package hydrocul.kametools.ls;

import java.io.File;

import hydrocul.kametest.Test;
import hydrocul.kametools.App;
import hydrocul.kametools.FileSet;

object LsAppTest {

  def test(){

    val env = new App.StringEnv();

    val file = new File("src").getAbsoluteFile;
    val fileSet = FileSet(file);
    val app = LsApp(fileSet);

    app.main(Array[String](), env);


  }

}
