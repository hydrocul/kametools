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

    val classPath = System.getProperty("java.class.path");
    val settings = new scala.tools.nsc.Settings;
    settings.classpath.value = classPath;
    val sout = new java.io.OutputStreamWriter(System.out);
    val interpreter = new scala.tools.nsc.InterpreterSifj(settings,
      new java.io.PrintWriter(sout));

    val result: InterpreterSifjResult = interpreter.interpretSifj(source, false);
    result.value match {
      case Some(v) => ObjectBank.put("$result", v.typeName, v.value);
      case None => ;
    }
    println(result.message);

  }

  def help(cmdName: String){
    // TODO
  }

}
