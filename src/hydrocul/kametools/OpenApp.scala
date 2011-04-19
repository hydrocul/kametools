package hydrocul.kametools;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import scala.collection.immutable.Queue;

case class OpenApp(fileSet: FileSet) extends App {

  override def exec(env: App.Env){
    val file = fileSet.head;
    OpenApp.openDefault(file, env);
  }

}

object OpenApp {

  def create(args: List[String]): App = {
    args match {
      case fname :: Nil =>
        OpenApp(LsApp.getFileSet(fname));
      case fname :: target :: Nil =>
        PutApp.getTarget(target) match {
          case Some(target) => PutApp(LsApp.getFileSet(fname), target);
          case None => throw new Exception("Unknown target: " + target);
        }
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

  def openDefault(file: File, env: App.Env){
    val desktop = Desktop.getDesktop;
    desktop.open(file);
    env.out.println("open: " + file.getAbsolutePath);
  }

}

