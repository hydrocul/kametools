package hydrocul.kametools.now;

import java.util.Date;

import hydrocul.kametools.App;
import hydrocul.kametools.Env;

object Now extends App {

  def main(cmdName: String, args: Array[String], env: Env){
    val now = new Date();
    val str = "%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS".format(now);
    println(str);
  }

  def help(cmdName: String){
    // TODO
  }

}
