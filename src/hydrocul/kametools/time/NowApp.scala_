package hydrocul.kametools.time;

import java.util.Date;

import hydrocul.kametools.App;

object NowApp extends App {

  override def exec(args: Array[String], env: App.Env){
    val now = new Date();
    val str = "%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS".format(now);
    env.out.println(str);
  }

  override def help(env: App.Env){

    env.out.println("display current time");
    env.out.println("usage: ");

  }

}
