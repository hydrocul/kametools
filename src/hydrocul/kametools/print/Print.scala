package hydrocul.kametools.print;

import java.io.File;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

case class Print(value: Any) extends App {

  override def main(cmdName: String, args: Array[String], env: App.Env){

    env.out.println(value.toString);

  }

  override def help(cmdName: String, env: App.Env){

    env.out.println("print object");
    env.out.println("usage: kt %s".format(cmdName));

  }

}
