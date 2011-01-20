package hydrocul.kametools.ls;

import java.io.File;
import java.util.regex.PatternSyntaxException;

import scala.util.matching.Regex;

import org.apache.commons.cli.{ Option => CliOption }
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import hydrocul.kametools.App;
import hydrocul.kametools.Env;
import hydrocul.kametools.ObjectBank;
import hydrocul.kametools.VirtualDirectory;

object Ls extends App {

  def main(cmdName: String, args: Array[String], env: Env){

    val options = new Options();
    options.addOption("r", false, "list subdirectories recursively");
    options.addOption( {
      val op = new CliOption("R", "recursive", true,
        "list subdirectories recursively");
      op.setArgName("depth");
      op;
    } );
    options.addOption("reverse", false, "reverse order while sorting");
    options.addOption( {
      val op = new CliOption("p", "pattern", true,
        "");
      op.setArgName("pattern");
      op;
    } );
    val parser = new PosixParser();
    val cli = try {
      parser.parse(options, args);
    } catch {
      case e: ParseException =>
        System.err.println(e.getMessage);
        return;
    }

    val depth = if(cli.hasOption("R")){
      try {
        cli.getOptionValue("R").toInt;
      } catch {
        case e: NumberFormatException =>
          System.err.println("Illegal argument for option: R " +
            cli.getOptionValue("R"));
          return;
      }
    } else if(cli.hasOption("r")){
      -1;
    } else {
      0;
    }

    val reverse: Boolean = cli.hasOption("reverse");

    val patterns: Option[String] = if(cli.hasOption("pattern")){
      Some(cli.getOptionValue("pattern"));
    } else {
      None;
    }

    val vd = VirtualDirectory.getArgFiles(cli.getArgs, Some("./"),
      false, true, env);
    val vd2 = LsVirtualDirectory.create(vd, depth, reverse, patterns);

    val list: Stream[File] = vd2.getList;

    var map = env.objectBank.getFiles;

    val s = env.objectBank.putFile(vd2, map);
    val vd2key = s._1;
    map = s._2;
    println("%s[%s]".format(vd2key, vd2));

    list.foreach { f: File =>
      val s = env.objectBank.putFile(f, map);
      println("%s %s".format(s._1, f));
      map = s._2;
    }

    env.objectBank.putFiles(map);

  }

  def help(cmdName: String){
    // TODO
  }

  case class LsVirtualDirectory(vd: VirtualDirectory,
    depth: Int, reverseOrder: Boolean,
    pattern: Option[String]) extends VirtualDirectory {

    override def getName = vd.getName +
      (if(depth==0) "" else " recursive (%d)".format(depth))
      (pattern match {
        case None => "";
        case Some(p) => " pattern: %s".format(p);
      } );

    override def getList = extractDir();

    override def getChild(path: String) = VirtualDirectory.empty;

    private def extractDir(): Stream[File] = {
      val l = if(reverseOrder) vd.getList.reverse else vd.getList;
      def cond(file: File): Boolean = {
        pattern match {
          case None => ;
          case Some(pattern) =>
            val name = file.getName;
            val patterns = pattern.split(" +");
            if(patterns.exists(p => name.indexOf(p) < 0))
              return false;
        }
        true;
      }
      extractDir(l, depth, reverseOrder, Stream.empty).
        filter(f => cond(f));
    }

    private def extractDir(files: Stream[File], depth: Int, reverseOrder: Boolean,
      next: => Stream[File]): Stream[File] = {

      if(files.isEmpty){
        next;
      } else {
        val f = files.head;
        lazy val t: Stream[File] = if(depth == 0){
          extractDir(files.tail, depth, reverseOrder, next);
        } else {
          val l: Stream[File] = VirtualDirectory.
            OneFileVirtualDirectory(f, true, reverseOrder).getList;
          extractDir(l, depth - 1, reverseOrder,
            extractDir(files.tail, depth, reverseOrder, next));
        }
        Stream.cons(f, t);
      }
    }

  }

  object LsVirtualDirectory {

    def create(vd: VirtualDirectory,
      depth: Int, reverseOrder: Boolean,
      pattern: Option[String]): VirtualDirectory = {

      if(depth==0 && !reverseOrder && pattern==None){
        vd;
      } else {
        new LsVirtualDirectory(vd, depth, reverseOrder, pattern);
      }
    }

  }

}
