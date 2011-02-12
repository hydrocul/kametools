package hydrocul.kametools.ls;

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

import hydrocul.kametools.App;
import hydrocul.kametools.FileSet;
import hydrocul.kametools.ObjectBank;

case class LsApp(fileSet: FileSet, count: Int = 50,
  timeFormat: String = "%Y-%m-%d-%H-%M-%S",
  lineFormat: String = "%1 %2  [%3]") extends App {

  override def exec(args: Array[String], env: App.Env){

    // 引数がある場合にそれを処理する App を取得する
    val nextApp: Option[App] = if(args.isEmpty){
      None;
    } else {
      (args(0), (if(args.length >= 2) Some(args(1)) else None)) match {
        case ("-T", Some(format)) =>
          Some(LsApp(fileSet, count, format, lineFormat));
        case (o, _) if(("-T" :: Nil).contains(o)) =>
          throw new Exception("No argument: " + o);
        case (o, _) =>
          throw new Exception("Unknown option: " + o);
      }
    }

    nextApp match {
      case Some(app) => app.main(args, env);
      case None => execSub(env);
    }

  }

  private def execSub(env: App.Env){

    val timeFormat2 = timeFormat.replaceAll("%", "%1\\$t");
    val lineFormat2 = lineFormat.replaceAll("%([1-9]+)", "%\\1\\$s");

    fileSet.foreach { file =>
      val key = ObjectBank.default.put(file);
      val t = new JDate(file.lastModified);
      val ts = timeFormat2.format(t);
      val s = lineFormat2.format(t, file, key);
      env.out.println(s);
    }

  }

  override def help(env: App.Env){

    env.out.println("display fileSet");
    env.out.println("usage: ");

  }


}
