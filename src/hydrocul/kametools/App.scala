package hydrocul.kametools;

trait App {

  def main(cmdName: String, args: Array[String], env: App.Env);

  def help(cmdName: String);

}

object App {

  class Env(val objectBank: ObjectBank);

}
