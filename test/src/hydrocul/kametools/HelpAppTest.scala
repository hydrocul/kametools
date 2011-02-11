package hydrocul.kametools;

import hydrocul.kametest.Test;

object HelpAppTest {

  def test(){

    val env = new App.StringEnv();

    val app = print.PrintApp("abc");
    app.main(Array("--help"), env);

    Test.assertEquals("", "print object\nusage: \n", env.getOutput());

  }

}
