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

  def create(args: List[String]): OpenApp = create(OpenApp(FileSet.empty), args);

  private def create(app: OpenApp, args: List[String]): OpenApp = {
    (app, args) match {
      case (OpenApp(fs), arg :: tail) if(fs.isEmpty) =>
        create(OpenApp(LsApp.getFileSet(arg)), tail);
      case (OpenApp(fs), Nil) if(!fs.isEmpty) => app;
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

  def openDefault(file: File, env: App.Env){
    val desktop = Desktop.getDesktop;
    desktop.open(file);
    env.out.println("open: " + file.getAbsolutePath);
  }

}

