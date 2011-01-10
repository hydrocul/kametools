package hydrocul.kametools.print;

import java.io.File;

import hydrocul.kametools.App;
import hydrocul.kametools.ObjectBank;

object Print extends App {

  def main(cmdName: String, args: Array[String], env: App.Env){

    args.foreach { a =>
      env.objectBank.load(a) match {
        case Some(ObjectBank.Field(typeName, value)) =>
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
    // TODO
  }

}
