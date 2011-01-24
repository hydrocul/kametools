package hydrocul.kametools;

import java.io.File;

trait App {

  def main(cmdName: String, args: Array[String], env: Env);

  def help(cmdName: String);

}

object App {

}
