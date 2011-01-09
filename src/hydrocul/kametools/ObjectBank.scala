package hydrocul.kametools;

class ObjectBank(dirName: String){

  import ObjectBank._;

  def load(name: String): Option[Field] = {
    (ioActor !? LoadAction(name)).asInstanceOf[Option[Field]];
  }

  def save(name: String, value: Option[Field]){
    ioActor !? SaveAction(name, value);
  }

  def getOrElse[A](name: String, defaultValue: =>A): A = {
    load(name) match {
      case Some(f) => f.getOrElse[A](defaultValue);
      case None => defaultValue;
    }
  }

  def put(name: String, typeName: String, value: AnyRef){
    save(name, Some(Field(typeName, value)));
  }

  def remove(name: String){
    save(name, None);
  }

  import java.io.File;
  import java.io.FileInputStream;
  import java.io.FileNotFoundException;
  import java.io.FileOutputStream;
  import java.io.IOException;
  import java.io.ObjectInputStream;
  import java.io.ObjectOutputStream;

  import scala.actors.Actor.loop
  import scala.actors.Actor.react;
  import scala.actors.Actor.reply;
  import scala.actors.DaemonActor;

  private case class LoadAction(name: String);
  private case class SaveAction(name: String, value: Option[Field]);

  private val ioActor = new DaemonActor(){ def act(){

    def load(name: String): Option[Field] = {
      try {
        val fname = dirName + File.separator + name;
        val fip = new FileInputStream(fname);
        val oip = new ObjectInputStream(fip);
        try {
          Some(oip.readObject().asInstanceOf[Field]);
        } finally {
          oip.close();
        }
      } catch {
        case e: FileNotFoundException => None;
        case e => e.printStackTrace(); None;
      }
    }

    def save(name: String, value: Option[Field]){
      value match {
        case Some(obj) =>
          try {
            (new File(dirName)).mkdirs();
            val fname = dirName + File.separator + name;
            val fop = new FileOutputStream(fname);
            val oop = new ObjectOutputStream(fop);
            try {
              oop.writeObject(obj);
            } finally {
              oop.flush();
              oop.close();
            }
          } catch {
            case e => e.printStackTrace();
          }
        case None =>
          try {
            val fname = dirName + File.separator + name;
            val fp = new File(fname);
            if(!fp.delete())
              throw new IOException("Cannot delete file:" + fname);
          } catch {
            case e => e.printStackTrace();
          }
      }
    }

    loop {
      react {
        case LoadAction(name) => reply(load(name));
        case SaveAction(name, value) => save(name, value); reply(true);
      }
    }

  }}; ioActor.start();

}

object ObjectBank {

  case class Field(typeName: String, value: AnyRef){

    def get[A]: A = value.asInstanceOf[A];

    def getOrElse[A](defaultValue: =>A): A = {
      try {
        value.asInstanceOf[A];
      } catch {
        case _ => defaultValue;
      }
    }

  }

}
