package hydrocul.kametools;

import java.io.File;

class ObjectBank(dirName: String){

  import ObjectBank._;

  def get(name: String): Option[(String, Any)] = {
    val r: (String, Option[Any]) = (ioActor !? LoadAction(name)).
      asInstanceOf[(String, Option[Any])];
  }

  def getOrElse[A](name: String, defaultValue: =>A): A = {
    get(name) match {
      case Some((_, v)) => try {
        v.asInstanceOf[A];
      } catch {
        case _ => defaultValue;
      }
      case None => defaultValue;
    }
  }

  def put(name: String, typeNameAndValue: Option[(String, Any)]){
    ioActor !? SaveAction(name, typeNameAndValue);
  }

  def put(name: String, typeName: String, value: Any){
    put(name, Some(typeName, value));
  }

  def remove(name: String){
    put(name, None);
  }

  def getFiles: Map[FileSet, String] = {
    getOrElse[Map[FileSet, String]](".files", Map());
  }

  def putFiles(fileMap: Map[FileSet, String]){
    put(".files", "scala.collection.immutable.Map[hydrocul.kametools.FileSet,java.lang.String]", fileMap);
  }

  def putFile(file: File, fileMap: Map[FileSet, String]): (String, Map[FileSet, String]) = {
    putFile(FileSet.OneFileSet(file), fileMap);
  }

  def putFile(fileSet: FileSet, fileMap: Map[FileSet, String]): (String, Map[FileSet, String]) = {

    def createRandom0(): String = {
      val first1 = "bcdfghjklmnpqrstvwxyz";
      val first2 = "aeiou";
      val first = first1.length * first2.length * first1.length;
      val second = first2.length * first1.length;
      val sum = first + second;
      val ret = new StringBuilder();
      val d = (math.random * sum);
      if(d < first){
        ret.append(first1.charAt((math.random * first1.length).asInstanceOf[Int]));
        ret.append(first2.charAt((math.random * first2.length).asInstanceOf[Int]));
        ret.append(first1.charAt((math.random * first1.length).asInstanceOf[Int]));
      } else {
        ret.append(first2.charAt((math.random * first2.length).asInstanceOf[Int]));
        val c = first1.charAt((math.random * first1.length).asInstanceOf[Int]);
        ret.append(c);
        ret.append(c);
      }
      ret.toString;
    }

    def createRandom(len: Int): String = {
      val ret = new StringBuilder();
      (1 to len).foreach { _ =>
        val r = (math.random * 26).asInstanceOf[Int];
        ret.append(('a' + r).asInstanceOf[Char]);
      }
      ret.toString;
    }

    def createName(level: Int): String = {
      val r = if(level < 2){
        createRandom0();
      } else if(level < 6){
        createRandom(3);
      } else {
        createRandom(4);
      }
      load(r) match {
        case None => r;
        case Some(_) => createName(level + 1);
      }
    }

    val ret: (String, Map[FileSet, String]) = {
      fileMap.get(fileSet) match {
        case Some(s) => (s, fileMap);
        case None =>
          val s = createName(0);
          (s, fileMap + (fileSet -> s));
      }
    }

    putFile("." + ret._1, fileSet, fileMap);

  }

  def putFile(name: String, fileSet: FileSet,
    fileMap: Map[FileSet, String]): (String, Map[FileSet, String]) = {

    val name2 = (if(name.startsWith("."))
      name.substring(1) else name);

    val newFileMap = fileMap + (fileSet -> name2);

    put(name, "hydrocul.kametools.FileSet", fileSet);

    (name2, newFileMap);

  }

  import java.io.BufferedReader;
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

  import groovy.lang.{Binding => GroovyBinding};
  import groovy.lang.GroovyShell;

  private case class LoadAction(name: String);
  private case class SaveAction(name: String,
    typeNameAndValue: Option[(String, Any)]);

  private val ioActor = new DaemonActor(){ def act(){

    def load(name: String): (String, Any) = {
      val fname = dirName + File.separator + name;
      try {
        if((new File(fname + ".txt")).exists){
          Some(loadString(fname));
        } else if((new File(fname + ".dat")).exists){
          Some(loadObject(fname));
        } else if((new File(fname + ".groovy")).exists){
          Some(loadGroovyObject(fname));
        } else if(!name.startsWith(".")){
          load("." + name);
        } else {
          None;
        }
      } catch {
        case e => e.printStackTrace(); None;
      }
    }

    def loadString(fname: String): (String, Any) = {
      ("java.lang.String", loadStringSub(fname, "-string.txt"));
    }

    def loadGroovyObject(fname: String): (String, Any) = {
      val source = loadStringSub(fname, ".groovy");
      val binding = new GroovyBinding();
      val shell = new GroovyShell(binding);
      val result = shell.evaluate(source);
      ("AnyRef", result);
    }

    def loadStringSub(fname: String, fnamePostfix: String): String = {
      val fip = new FileInputStream(fname + fnamePostfix);
      val reader = new BufferedReader(new InputStreamReader(fip));
      val buf = new JStringBuilder();
      try {
        val buf2 = new Array[Char](1024);
        var len = reader.read(buf2, 0, buf2.length);
        while(len >= 0){
          buf.append(buf2, 0, len);
          len = reader.read(buf2, 0, buf2.length);
        }
        buf.toString;
      } finally {
        reader.close();
      }
    }

    def loadObject(fname: String): (String, Any) = {
      val fip = new FileInputStream(fname + ".dat");
      val oip = new ObjectInputStream(fip);
      try {
        val typeName = oip.readUTF();
        val value = oip.readObject();
        (typeName, value);
      } finally {
        oip.close();
      }
    }

    def save(name: String, typeNameAndValue: Option[(String, Any)]){
      val fname = dirName + File.separator + name;
      try {
        remove(fname);
        typeNameAndValue match {
          case Some(tv) =>
            (new File(dirName)).mkdirs();
            tv match {
              case ("java.lang.String", str: String) =>
                saveString(fname, str);
              case (typeName, value) =>
                saveObject(fname, typeName, value);
            }
          case None => ;
        }
      } catch {
        case e => e.printStackTrace();
      }
    }

    def saveString(fname: String, str: String){
      saveStringSub(fname, "-string.txt", str);
    }

    def saveStringSub(fname: String, fnamePostfix: String, str: String){
      val fop = new FileOutputStream(fname + fnamePostfix);
      val writer = new OutputStreamWriter(fop);
      try {
        writer.write(str);
      } finally {
        writer.close();
      }
    }

    def saveObject(fname: String, typeName: String, value: Any){
      val fop = new FileOutputStream(fname + ".dat");
      val oop = new ObjectOutputStream(fop);
      try {
        oop.writeUTF(typeName);
        oop.writeObject(value);
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
        case SaveAction(name, typeName, value) =>
          save(name, typeName, value); reply(true);
      }
    }

  }};
  ioActor.start();

}

object ObjectBank {

  def get(name: String): Option[(String, Any)] = {
    default.get(name);
  }

  def getOrElse[A](name: String, defaultValue: =>A): A = {
    default.getOrElse(name, defaultValue);
  }

  def put(name: String, typeNameAndValue: Option[(String, Any)]){
    default.save(name, value);
  }

  def put(name: String, typeName: String, value: Any){
    default.put(name, typeName, value);
  }

  def remove(name: String){
    default.remove(name);
  }

  def getFiles: Map[FileSet, String] = {
    default.getFiles;
  }

  def putFiles(fileMap: Map[FileSet, String]){
    default.putFiles(fileMap);
  }

  def putFile(file: File, fileMap: Map[FileSet, String]): (String, Map[FileSet, String]) = {
    default.putFile(file, fileMap);
  }

  def putFile(fileSet: FileSet, fileMap: Map[FileSet, String]): (String, Map[FileSet, String]) = {
    default.putFile(fileSet, fileMap);
  }

  def putFile(name: String, fileSet: FileSet,
    fileMap: Map[FileSet, String]): (String, Map[FileSet, String]) = {
    default.putFile(name, fileSet, fileMap);
  }

  private lazy val default: ObjectBank = new ObjectBank(getDirName());

  private def getDirName(): String = {
    import java.io.File;
    System.getProperty("user.home") + File.separator + ".kametools";
  }

}
