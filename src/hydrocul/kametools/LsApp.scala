package hydrocul.kametools;

import java.io.File;
import java.util.{ Date => JDate }
import java.util.NoSuchElementException;
import java.util.regex.PatternSyntaxException;

import scala.util.matching.Regex;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.{ Option => CliOption }
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

case class LsApp(fileSet: FileSet, count: Int = 50,
  timeFormat: String = "%Y-%m-%d-%H%M%S",
  lineFormat: String = "%2  [%3]") extends App {

  override def exec(env: App.Env){

    val timeFormat2 = timeFormat.replaceAll("%", "%1\\$t");
    val lineFormat2 = lineFormat.replaceAll("%([1-9]+)", "%$1\\$s");

    def printFile(file: File){
      val key = ObjectBank.default.put(file);
      val t = new JDate(file.lastModified);
      val ts = timeFormat2.format(t);
      val s = lineFormat2.format(ts, file, key, file.getName);
      env.out.println(s);
    }

    if(count <= 0){
      fileSet.foreach { printFile _ }
    } else {
      var i = 0;
      var fs = fileSet;
      var existsNext = !fs.isEmpty;
      while(existsNext){
        printFile(fs.head);
        i = i + 1;
        fs = fs.tail;
        if(i >= count){
          val more = ObjectBank.default.put(fs);
          env.out.println("show more: [%s]".format(more));
          existsNext = false;
        } else {
          existsNext = !fs.isEmpty;
        }
      }
    }

  }

}

object LsApp {

  def create(args: List[String]): LsApp = create(LsApp(FileSet.empty), false, args);

  private def create(app: LsApp, argExists: Boolean, args: List[String]): LsApp = {
    args match {
      case "-t" :: format :: tail => create(LsApp(app.fileSet, app.count,
        format, app.lineFormat), argExists, tail);
      case "-f" :: format :: tail => create(LsApp(app.fileSet, app.count,
        app.timeFormat, format), argExists, tail);
      case "-a" :: tail => create(LsApp(app.fileSet, 0,
        app.timeFormat, app.lineFormat), argExists, tail);
      case "-R" :: tail => create(LsApp(FileSet.recursive(app.fileSet, 0, -1,
        false, false), app.count, app.timeFormat, app.lineFormat), argExists, tail);
      case OptionCPattern(d) :: tail => create(LsApp(app.fileSet, d.toInt,
        app.timeFormat, app.lineFormat), argExists, tail);
      case OptionRPattern1(d1) :: tail => create(LsApp(app.fileSet.recursive(
        0, d1.toInt, false, false), app.count,
        app.timeFormat, app.lineFormat), argExists, tail);
      case OptionRPattern2(d1, d2) :: tail => create(LsApp(app.fileSet.recursive(
        d1.toInt, d2.toInt, false, false), app.count,
        app.timeFormat, app.lineFormat), argExists, tail);
      case OptionRPattern3(d1) :: tail => create(LsApp(app.fileSet.recursive(
        d1.toInt, -1, false, false), app.count,
        app.timeFormat, app.lineFormat), argExists, tail);
      case "-p" :: pattern :: tail => create(LsApp(app.fileSet.filter(cond(_, pattern)),
        app.count, app.timeFormat, app.lineFormat), argExists, tail);
      case "-r" :: tail => create(LsApp(app.fileSet.reverse, app.count,
        app.timeFormat, app.lineFormat), argExists, tail);
      case arg :: tail => create(LsApp(FileSet.concat(app.fileSet, getFileSet(arg)),
        app.count, app.timeFormat, app.lineFormat), true, tail);
      case Nil => if(argExists) app else LsApp(FileSet(new File("./")).getChildren,
        app.count, app.timeFormat, app.lineFormat);
    }
  }

  private def cond(file: File, pattern: String): Boolean = {
    val name = file.getName;
    val patterns = pattern.split(" +");
    !patterns.exists(p => name.indexOf(p) < 0);
  }

  def getFileSet(arg: String): FileSet = {
    val (a, isDir) = if(arg.endsWith("/"))
      (arg.substring(0, arg.length - 1), true)
    else
      (arg, false);
    val f = new File(a);
    val fs = if(f.exists){
      FileSet(f);
    } else {
      ObjectBank.default.get(a) match {
        case Some(f: FileSet) => f;
        case Some(f: File) => FileSet(f);
        case _ => throw new Exception("Not Found: " + a);
      }
    }
    if(isDir) fs.getChildren else fs;
  }

  private val OptionCPattern = "-c(\\d+)".r;

  private val OptionRPattern1 = "-R-?(\\d+)".r;

  private val OptionRPattern2 = "-R(\\d+)-(\\d+)".r;

  private val OptionRPattern3 = "-R(\\d+)-".r;

}

