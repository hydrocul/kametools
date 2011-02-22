package hydrocul.kametools;

import hydrocul.kametest.Test;

object PrintAppTest {

  def test(){

    val env = new App.StringEnv();

    val app = PrintApp("abc");
    app.exec(env);

    Test.assertEquals("", "abc\n", env.getOutput());

  }

}
