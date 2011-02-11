package hydrocul.kametools.print;

import java.io.File;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object Print extends App {

  def main(cmdName: String, args: Array[String]){

    args.foreach { a =>
      ObjectBank.get(a) match {
        case Some((typeName, value)) =>
          print(a);
          print(": ");
          print(typeName);
          print(" = ");
          println(value.toString);
        case None =>
          println("not found: " + a);
      }
    }

  }

  def help(cmdName: String){
    println("print object");
    println("usage: kt print <name>");
  }

}
