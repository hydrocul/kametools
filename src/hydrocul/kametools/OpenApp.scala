package hydrocul.kametools;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

case class OpenApp(fileSet: FileSet) extends App {

  override def exec(env: App.Env){
    val file = fileSet.head;
    val desktop = Desktop.getDesktop;
    desktop.open(file);
  }

}


