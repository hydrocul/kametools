package hydrocul.kametools.print;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;

import hydrocul.kametest.Test;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object PrintAppTest {

  def test(){

    val env = new App.StringEnv();

    val app = PrintApp("abc");
    app.main(Array(), env);

    Test.assertEquals("", "abc\n", env.getOutput());

  }

}
