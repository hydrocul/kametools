package hydrocul.kametools.print;

import hydrocul.kametest.Test;

import hydrocul.kametools.App;

object PrintAppTest {

  def test(){

    val env = new App.StringEnv();

    val app = PrintApp("abc");
    app.main(Array(), env);

    Test.assertEquals("", "abc\n", env.getOutput());

  }

}
