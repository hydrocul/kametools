package hydrocul.kametools;

import java.io.File;

case class PrintApp(value: Any) extends App {

  override def exec(env: App.Env){
    env.out.println(value.toString);
  }

  override def help(env: App.Env){

    env.out.println("print object");
    env.out.println("usage: ");

  }

}
