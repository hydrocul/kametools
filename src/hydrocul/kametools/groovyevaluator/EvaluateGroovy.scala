package hydrocul.kametools.groovyevaluator;

import java.io.File;

import groovy.lang.{Binding => GroovyBinding};
import groovy.lang.GroovyShell;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object EvaluateGroovy extends App {

  def main(cmdName: String, args: Array[String]){

    val source = args.mkString(" ");

    val result = evaluate(source);

    result._1 match {
      case Some(v) => ObjectBank.put("$result", v._1, v._2);
      case None => ;
    }
    println(result._2);

  }

  private def evaluate(source: String): (Option[(String, Any)], String) = {

    val binding = new GroovyBinding();
    val shell = new GroovyShell(binding);

    val source2 = source.replaceAll("\\$([$a-zA-Z][$a-zA-Z0-9]*)",
      "ob['$1']");
    val source3 = if(source2==source) source else {
      "import hydrocul.kametools.ob; " + source2;
    }

    val result = shell.evaluate(source3);
    val msg = if(result==null) "" else result.toString;

    (Some(("AnyRef", result)), msg);

  }

  def help(cmdName: String){
    // TODO
  }

}
