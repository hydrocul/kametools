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
import hydrocul.kametools.FileSet;
import hydrocul.kametools.ObjectBank;

object Ls extends App {

  def main(cmdName: String, args: Array[String]){

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
    options.addOption( {
      val op = new CliOption("T", "time", true,
        "");
      op.setArgName("format");
      op;
    } );
    options.addOption( {
      val op = new CliOption("f", "format", true,
        "");
      op.setArgName("format");
      op;
    } );
    options.addOption( {
      val op = new CliOption("l", "label", true,
        "");
      op.setArgName("label");
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

    val printTimeFormat: Option[String] = if(cli.hasOption("T")){
      Some(cli.getOptionValue("T"));
    } else {
      None;
    }

    val printFormat: Option[String] = if(cli.hasOption("f")){
      Some(cli.getOptionValue("f"));
    } else {
      None;
    }

    val label: Option[String] = if(cli.hasOption("l")){
      Some(cli.getOptionValue("l"));
    } else {
      None;
    }

    val vd = FileSet.getArgFiles(cli.getArgs, Some("./"),
      false, true);
    val vd2 = LsFileSet.create(vd, depth, reverse, patterns);

    val list: Stream[File] = vd2.toStream;

    var map = ObjectBank.getFiles;

    val s = label match {
      case Some(l) => ObjectBank.putFile(l, vd2, map);
      case None => ObjectBank.putFile(vd2, map);
    }
    val vd2key = s._1;
    map = s._2;
    if(!vd2.isSingleFile){
      println("%s [%s]".format(vd2key, vd2));
    }

    list.foreach { f: File =>
      val s = ObjectBank.putFile(f, map);
      printFileInfo(s._1, f, printTimeFormat, printFormat)
      map = s._2;
    }

    ObjectBank.putFiles(map);

  }

  def help(cmdName: String){
    // TODO
  }

  private def printFileInfo(key: String, file: File,
    printTimeFormat: Option[String], printFormat: Option[String]){
    val time = new java.util.Date(file.lastModified);
    val tf = printTimeFormat match {
      case Some(f) => f.replaceAll("%", "%1\\$t");
      case None => "%1$tY-%1$tm-%1$td-%1$tH-%1$tM-%1$tS";
    }
    val t = tf.format(time);
    val f = printFormat match {
      case Some(f) => f;
      case None => "%1$s %2$s %3$s";
    }
    println(f.format(t, key, file));
  }

  case class LsFileSet(vd: FileSet,
    depth: Int, reverseOrder: Boolean,
    pattern: Option[String]) extends FileSet {

    override lazy val name = vd.name +
      (if(depth==0) "" else " recursive (%d)".format(depth))
      (pattern match {
        case None => "";
        case Some(p) => " pattern: %s".format(p);
      } );

    @transient private var _stream: Stream[File] = null;
    private def stream: Stream[File] = {
      if(_stream==null){
        synchronized {
          if(_stream==null){
            _stream = $stream;
          }
        }
      }
      _stream;
    }
    private def $stream = extractDir();

    override def isEmpty = stream.isEmpty;

    override def head = stream.head;

    override def tail = FileSet.StreamFileSet.create(name, stream.tail);

    private def extractDir(): Stream[File] = {
      val l = if(reverseOrder) vd.toStream.reverse else vd.toStream;
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
          val l: Stream[File] = FileSet.
            DirFileSet(f, reverseOrder).toStream;
          extractDir(l, depth - 1, reverseOrder,
            extractDir(files.tail, depth, reverseOrder, next));
        }
        Stream.cons(f, t);
      }
    }

  }

  object LsFileSet {

    def create(vd: FileSet,
      depth: Int, reverseOrder: Boolean,
      pattern: Option[String]): FileSet = {

      if(depth==0 && !reverseOrder && pattern==None){
        vd;
      } else {
        new LsFileSet(vd, depth, reverseOrder, pattern);
      }
    }

  }

}
