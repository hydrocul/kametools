package hydrocul.kametools;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import scala.collection.immutable.Queue;

case class OpenApp(fileSet: FileSet) extends App {

  override def exec(env: App.Env){
    val file = fileSet.head;
    OpenApp.open(file, env);
  }

}

object OpenApp {

  object FileOpenReceiver extends App {

    override def exec(env: App.Env){
      OpenApp.wait(env);
    }

  }

  private val configName = "fileOpener";
  // expected XML, for example:
  // <fileOpener>
  //   <entry pattern="/mnt/cdrive/(.*\.xls)" replace="C:\\$1"
  //     target="/mnt/cdrive/Users/foo/.kametools" />
  //   <entry pattern="/mnt/cdrive/(.*\.doc)" replace="C:\\$1"
  //     target="/mnt/cdrive/Users/foo/.kametools" />
  // </fileOpener>  

  private lazy val configXml = ObjectBank.default.getXmlOrElse(configName,
    <fileOpener></fileOpener>);

  private def open(file: File, env: App.Env){
    (configXml \ "entry").find({ entry =>
      val pattern = java.util.regex.Pattern.compile((entry \ "@pattern").text);
      val matcher = pattern.matcher(file.getAbsolutePath);
      if(matcher.matches()){
        matcher.reset();
        val p = matcher.replaceFirst((entry \ "@replace").text);
        val t = (entry \ "@target").text;
        putIndication(t, p);
        env.out.println("open: " + file.getAbsolutePath);
        env.out.println("in %s as %s".format(t, p));
        true;
      } else {
        false;
      }
    }) match {
      case Some(_) => ;
      case None => openDefault(file, env);
    }
  }

  private def openDefault(file: File, env: App.Env){
    val desktop = Desktop.getDesktop;
    desktop.open(file);
    env.out.println("open: " + file.getAbsolutePath);
  }

  private def wait(env: App.Env){
    clearIndication();
    while(true){
      getIndication() match {
        case Some(filePath) =>
          open(new File(filePath), env);
          Thread.sleep(500);
        case None =>
          Thread.sleep(500);
      }
    }
  }

  private val varName = ".openFile";

  /**
   * obDirName に filePath を開く指示を書き込む。
   */
  private def putIndication(obDirName: String, filePath: String){
    val ob = new ObjectBank(obDirName);
    val q: Queue[String] = ob.getOrElse[Queue[String]](varName, Queue());
    ob.put(varName, Some(q :+ filePath));
  }

  /**
   * ファイルを開く指示を1つ抜き出す。
   */
  private def getIndication(): Option[String] = {
    ObjectBank.default.get(varName) match {
      case Some(q: Queue[_]) if(!q.isEmpty) => {
        q.head match {
          case h: String =>
            ObjectBank.default.put(varName, Some(q.tail));
            Some(h);
          case _ =>
            None;
        }
      }
      case _ => None;
    }
  }

  private def clearIndication(){
    ObjectBank.default.put(varName, Some(Queue()));
  }

}

