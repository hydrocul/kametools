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

  import java.io.BufferedReader;
  import java.io.File;
  import java.io.FileInputStream;
  import java.io.FileNotFoundException;
  import java.io.FileOutputStream;
  import java.io.InputStreamReader;
  import java.io.IOException;
  import java.io.ObjectInputStream;
  import java.io.ObjectOutputStream;
  import java.io.OutputStreamWriter;
  import java.lang.{StringBuilder => JStringBuilder}

  import scala.actors.Actor.loop
  import scala.actors.Actor.react;
  import scala.actors.Actor.reply;
  import scala.actors.DaemonActor;

  private case class LoadAction(name: String);
  private case class SaveAction(name: String, value: Option[Field]);

  private val ioActor = new DaemonActor(){ def act(){

    def load(name: String): Option[Field] = {
      val fname = dirName + File.separator + name;
      try {
        if((new File(fname + ".txt")).exists){
          Some(loadString(fname));
        } else if((new File(fname + ".dat")).exists){
          Some(loadObject(fname));
        } else {
          None;
        }
      } catch {
        case e => e.printStackTrace(); None;
      }
    }

    def loadString(fname: String): Field = {
      val fip = new FileInputStream(fname + "-string.txt");
      val reader = new BufferedReader(new InputStreamReader(fip));
      val buf = new JStringBuilder();
      try {
        val buf2 = new Array[Char](1024);
        var len = reader.read(buf2, 0, buf2.length);
        while(len >= 0){
          buf.append(buf2, 0, len);
          len = reader.read(buf2, 0, buf2.length);
        }
        Field("java.lang.String", buf.toString);
      } finally {
        reader.close();
      }
    }

    def loadObject(fname: String): Field = {
      val fip = new FileInputStream(fname + ".dat");
      val oip = new ObjectInputStream(fip);
      try {
        oip.readObject().asInstanceOf[Field];
      } finally {
        oip.close();
      }
    }

    def save(name: String, value: Option[Field]){
      val fname = dirName + File.separator + name;
      try {
        remove(fname);
        value match {
          case Some(obj) =>
            (new File(dirName)).mkdirs();
            obj match {
              case Field("java.lang.String", str: String) =>
                saveString(fname, str);
              case field =>
                saveObject(fname, field);
            }
          case None => ;
        }
      } catch {
        case e => e.printStackTrace();
      }
    }

    def saveString(fname: String, str: String){
      val fop = new FileOutputStream(fname + ".txt");
      val writer = new OutputStreamWriter(fop);
      try {
        writer.write(str);
      } finally {
        writer.close();
      }
    }

    def saveObject(fname: String, field: Field){
      val fop = new FileOutputStream(fname + ".dat");
      val oop = new ObjectOutputStream(fop);
      try {
        oop.writeObject(field);
      } finally {
        oop.close();
      }
    }

    def remove(fname: String){
      var f = false;
      val fp1 = new File(fname + ".dat");
      if(fp1.exists && !fp1.delete())
        f = true;
      val fp2 = new File(fname + ".txt");
      if(fp2.exists && !fp2.delete())
        f = true;
      if(f)
        throw new IOException("Cannot delete file:" + fname + ".*");
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
