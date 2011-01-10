package hydrocul.kametools.groovy;

import java.io.File;

import groovy.lang.{Binding => GroovyBinding};
import groovy.lang.GroovyShell;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object EvaluateGroovy extends App {

  def main(cmdName: String, args: Array[String], env: App.Env){

    val source = args.mkString(" ");
    val binding = new GroovyBinding();
    val shell = new GroovyShell(binding);
    val result = shell.evaluate(source);
    env.objectBank.put("$result", "AnyRef", result);
    println(result);

  }

  def help(cmdName: String){
    // TODO
  }

}
