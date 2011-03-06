package hydrocul.kametools;

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
import scala.xml.Elem;
import scala.xml.XML;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import groovy.lang.{Binding => GroovyBinding};
import groovy.lang.GroovyShell;

class ObjectBank(dirName: String){

  import ObjectBank._;

  private val log = LogFactory.getLog(this.getClass);

  def get(name: String): Option[AnyRef] = {
    val ret = (ioActor !? LoadAction(name)).
      asInstanceOf[Option[AnyRef]];
    ret match {
      case None if(!name.startsWith(".")) => get("." + name);
      case _ => ret;
    }
  }

  def getOrElse[A](name: String, defaultValue: =>A): A = {
    get(name) match {
      case Some(v) => try {
        v.asInstanceOf[A];
      } catch {
        case _ => defaultValue;
      }
      case _ => defaultValue;
    }
  }

  def getNameByValue(value: AnyRef): Option[String] = {
    val hashName = getHashName(value);
    val list = getOrElse[List[String]](hashName, Nil);
    val name: Option[String] = list.find { s =>
      get(s) match {
        case Some(v) if(v==value) => true;
        case _ => false;
      }
    }
    name match {
      case Some(name) if(name.startsWith(".")) => Some(name.substring(1));
      case _ => name;
    }
  }

  def getXml(name: String): Option[Elem] = {
    get(name) match {
      case Some(s: String) => Some(XML.loadString(s));
      case _ => None;
    }
  }

  def getXmlOrElse(name: String, defaultValue: =>Elem): Elem = {
    getXml(name) match {
      case Some(elem) => elem;
      case None => defaultValue;
    }
  }

  def put(name: String, value: Option[AnyRef]){
    var changed = true;
    get(name).foreach { oldValue =>
      if(oldValue == value){
        changed = false;
      } else {
        val hashName = getHashName(oldValue);
        val list = getOrElse[List[String]](hashName, Nil);
        val newList = list.filter(s => s != name);
        putRaw(hashName, Some(newList));
      }
    }
    if(changed){
      putRaw(name, value);
    }
    value.foreach { value =>
      val hashName = getHashName(value);
      val list = getOrElse[List[String]](hashName, Nil);
      val newList = name :: list;
      putRaw(hashName, Some(newList));
    }
  }

  /**
   * returns name.
   */
  def put(value: AnyRef): String = {
    val name: String = getNameByValue(value) match {
      case Some(name) => name;
      case None => "." + createRandomName;
    }
    put(name, Some(value));
    if(name.startsWith(".")) name.substring(1) else name;
  }

  private def putRaw(name: String, value: Option[AnyRef]){
    ioActor !? SaveAction(name, value);
  }

  def remove(name: String){
    put(name, None);
    if(!name.startsWith(".")) put("." + name, None);
  }

  private def createRandomName: String = {

    def create1: String = {
      // 21 * 5 * 21 patterns
      val first1 = "bcdfghjklmnpqrstvwxyz";
      val first2 = "aeiou";
      val ret = new StringBuilder();
      ret.append(first1.charAt((math.random * first1.length).
        asInstanceOf[Int]));
      ret.append(first2.charAt((math.random * first2.length).
        asInstanceOf[Int]));
      ret.append(first1.charAt((math.random * first1.length).
        asInstanceOf[Int]));
      ret.toString;
    }

    def create2: String = {
      // 21 * 5 patterns
      val first1 = "bcdfghjklmnpqrstvwxyz";
      val first2 = "aeiou";
      val ret = new StringBuilder();
      ret.append(first2.charAt((math.random * first2.length).
        asInstanceOf[Int]));
      val c = first1.charAt((math.random * first1.length).
        asInstanceOf[Int]);
      ret.append(c);
      ret.append(c);
      ret.toString;
    }

    def create3(len: Int): String = {
      val ret = new StringBuilder();
      (1 to len).foreach { _ =>
        val r = (math.random * 26).asInstanceOf[Int];
        ret.append(('a' + r).asInstanceOf[Char]);
      }
      ret.toString;
    }

    def create(level: Int): String = {
      val d1 = 21 * 5 * 21;
      val d2 = 21 * 5;
      val name = level match {
        case level if(level < 4) =>
          if(math.random * (d1 + d2) < d1){
            create1;
          } else {
            create2;
          }
        case level =>
          create3((level - 4) / 4 + 3);
      }
      get(name) match {
        case None => name;
        case Some(_) => create(level + 1);
      }
    }

    create(0);

  }



  private case class LoadAction(name: String);
  private case class SaveAction(name: String, value: Option[AnyRef]);

  private val ioActor = new DaemonActor(){ def act(){

    case class LoggedException(e: Throwable) extends Exception;

    def load(name: String): Option[AnyRef] = {
      val fnameBody = dirName + File.separator + name;
      try {
        if((new File(fnameBody + "-string.txt")).exists){
          Some(loadString(fnameBody));
        } else if((new File(fnameBody + ".groovy")).exists){
          Some(loadGroovyObject(fnameBody));
        } else if((new File(fnameBody + ".dat")).exists){
          Some(loadObject(fnameBody));
        } else {
          None;
        }
      } catch {
        case LoggedException(e) => None;
        case e => log.error("load error name=" + name, e);
          None;
      }
    }

    def loadString(fnameBody: String): AnyRef = {
      loadStringSub(fnameBody, "-string.txt");
    }

    def loadGroovyObject(fnameBody: String): AnyRef = {
      val source = loadStringSub(fnameBody, ".groovy");
      try {
        val binding = new GroovyBinding();
        val shell = new GroovyShell(binding);
        val result = shell.evaluate(source);
        result;
      } catch {
        case e => log.error("groovy execution error, fname=" +
          fnameBody + ".groovy", e);
          throw LoggedException(e);
      }
    }

    def loadStringSub(fnameBody: String, fnamePostfix: String): String = {
      val fip = new FileInputStream(fnameBody + fnamePostfix);
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
      } catch {
        case e => log.error("load error, fname=" +
          fnameBody + fnamePostfix, e);
          throw LoggedException(e);
      } finally {
        reader.close();
      }
    }

    def loadObject(fnameBody: String): AnyRef = {
      val fip = new FileInputStream(fnameBody + ".dat");
      val oip = new ObjectInputStream(fip);
      try {
        val value = oip.readObject();
        value;
      } catch {
        case e => log.error("load error, fname=" +
          fnameBody + ".dat", e);
          throw LoggedException(e);
      } finally {
        oip.close();
      }
    }

    def save(name: String, value: Option[AnyRef]){
      val fnameBody = dirName + File.separator + name;
      try {
        remove(fnameBody);
        value match {
          case Some(value) =>
            (new File(dirName)).mkdirs();
            value match {
              case str: String =>
                saveString(fnameBody, str);
              case value =>
                saveObject(fnameBody, value);
            }
          case None => ;
        }
      } catch {
        case LoggedException(e) => None;
        case e => log.error("save error name=" + name, e);
          None;
      }
    }

    def saveString(fnameBody: String, str: String){
      saveStringSub(fnameBody, "-string.txt", str);
    }

    def saveStringSub(fnameBody: String, fnamePostfix: String, str: String){
      val fop = new FileOutputStream(fnameBody + fnamePostfix);
      val writer = new OutputStreamWriter(fop);
      try {
        writer.write(str);
      } catch {
        case e => log.error("save error, fname=" +
          fnameBody + fnamePostfix, e);
          throw LoggedException(e);
      } finally {
        writer.close();
      }
    }

    def saveObject(fnameBody: String, value: Any){
      val fop = new FileOutputStream(fnameBody + ".dat");
      val oop = new ObjectOutputStream(fop);
      try {
        oop.writeObject(value);
      } catch {
        case e => log.error("save error, fname=" +
          fnameBody + ".dat", e);
          throw LoggedException(e);
      } finally {
        oop.close();
      }
    }

    def remove(fnameBody: String){
      var f = false;
      val fp1 = new File(fnameBody + "-string.txt");
      if(fp1.exists && !fp1.delete())
        f = true;
      val fp2 = new File(fnameBody + ".groovy");
      if(fp2.exists && !fp2.delete())
        f = true;
      val fp3 = new File(fnameBody + ".dat");
      if(fp3.exists && !fp3.delete())
        f = true;
      if(f)
        throw new IOException("Cannot delete file:" + fnameBody + ".*");
    }

    loop {
      react {
        case LoadAction(name) =>
          reply(load(name));
        case SaveAction(name, value) =>
          save(name, value);
          reply(true);
      }
    }

  }}
  ioActor.start();

}

object ObjectBank {

  lazy val default: ObjectBank = new ObjectBank(dirName);

  def dirName: String = {
    System.getProperty("user.home") + File.separator + ".kametools";
  }

  object forScala {

    def apply(name: String): AnyRef = {
      default.get(name) match {
        case Some(v) => v;
        case None => throw new NoSuchElementException("key: " + name);
      }
    }

    def update(name: String, value: AnyRef){
      default.put(name, Some(value));
    }

  }

  object forGroovy {

    def get(name: String): AnyRef = {
      forScala.apply(name);
    }

    def put(name: String, value: AnyRef){
      forScala.update(name, value);
    }

  }

  private def getHashCode(obj: AnyRef): Int = obj.hashCode;

  private def getHashName(value: AnyRef): String = ".%02d".format(
    getHashCode(value) % 100);

}
