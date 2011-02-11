package hydrocul.kametools.print;

import java.io.File;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

case class PrintApp(value: Any) extends App {

  override def exec(args: Array[String], env: App.Env){

    env.out.println(value.toString);

  }

  override def help(env: App.Env){

    env.out.println("print object");
    env.out.println("usage: ");

  }

}
