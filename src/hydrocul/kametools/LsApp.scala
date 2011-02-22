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
  timeFormat: String = "%Y-%m-%d-%H-%M-%S",
  lineFormat: String = "%1 %2  [%3]") extends App {

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

  override def next(arg: String): App = (nextCommonly(arg), arg) match {
    case (Some(app), _) => app;
    case (None, "-t") => App.NeedOfArgumentApp(
      format => LsApp(fileSet, count, format, lineFormat));
    case (None, "-f") => App.NeedOfArgumentApp(
      format => LsApp(fileSet, count, timeFormat, format));
    case (None, LsApp.OptionCPattern(c)) => LsApp(fileSet,
      c.toInt, timeFormat, lineFormat);
    case (None, "-a") => LsApp(fileSet, 0, timeFormat, lineFormat);
    case (None, "-r") => LsApp(
      FileSet.recursive(fileSet, 0, -1, false, false),
      count, timeFormat, lineFormat);
    case (None, LsApp.OptionRPattern1(d)) => LsApp(
      FileSet.recursive(fileSet, 0, d.toInt, false, false),
      count, timeFormat, lineFormat);
    case (None, LsApp.OptionRPattern2(d1, d2)) => LsApp(
      FileSet.recursive(fileSet, d1.toInt, d2.toInt, false, false),
      count, timeFormat, lineFormat);
    case (None, LsApp.OptionRPattern3(d1)) => LsApp(
      FileSet.recursive(fileSet, d1.toInt, -1, false, false),
      count, timeFormat, lineFormat);
    case (None, "-v") => LsApp(fileSet.reverse,
      count, timeFormat, lineFormat);
    case (None, "-p") => App.NeedOfArgumentApp(
      pattern => LsApp(fileSet.filter(cond(_, pattern)), count, timeFormat, lineFormat));
    case (None, "-o") => OpenApp(fileSet);
    case _ => throw new Exception("Unknown option: " + arg);
  }

  private def cond(file: File, pattern: String): Boolean = {
    val name = file.getName;
    val patterns = pattern.split(" +");
    !patterns.exists(p => name.indexOf(p) < 0);
  }

}

object LsApp {

  private val OptionCPattern = "-c(\\d+)".r;

  private val OptionRPattern1 = "-r-?(\\d+)".r;

  private val OptionRPattern2 = "-r(\\d+)-(\\d+)".r;

  private val OptionRPattern3 = "-r(\\d+)-".r;

}

