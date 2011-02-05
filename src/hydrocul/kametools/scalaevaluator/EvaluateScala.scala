package hydrocul.kametools.scalaevaluator;

import java.io.File;
import java.io.OutputStreamWriter;

import scala.tools.nsc.InterpreterSifjResult;
import scala.tools.nsc.InterpreterResults;
import scala.tools.nsc.ResultValueInfo;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object EvaluateScala extends App {

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

    val classPath = System.getProperty("java.class.path");
    val settings = new scala.tools.nsc.Settings;
    settings.classpath.value = classPath;
    val sout = new java.io.OutputStreamWriter(System.out);
    val interpreter = new scala.tools.nsc.InterpreterSifj(settings,
      new java.io.PrintWriter(sout));

    val source2 = source.replaceAll("\\$([$a-zA-Z][$a-zA-Z0-9]*)",
      "hydrocul.kametools.ob(\"$1\")");

    val result: InterpreterSifjResult = interpreter.interpretSifj(
      source2, false);
    result.value match {
      case Some(v) => (Some((v.typeName, v.value)), result.message);
      case None => (None, result.message);
    }

  }

  def help(cmdName: String){
    // TODO
  }

}
