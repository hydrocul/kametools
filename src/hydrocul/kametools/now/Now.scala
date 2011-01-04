package hydrocul.kametools.now;

import java.util.Date;

object Now {

  def main(args: Array[String]){
    val now = new Date();
    val str = "%1$tY/%1$tm/%1$td %1$tH:%1$tM:%1$tS".format(now);
    println(str);
  }

}
