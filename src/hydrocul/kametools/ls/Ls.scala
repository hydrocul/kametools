package hydrocul.kametools.ls;

import java.io.File;
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
import hydrocul.kametools.Env;
import hydrocul.kametools.FileSet;
import hydrocul.kametools.ObjectBank;

object Ls extends App {

  def main(cmdName: String, args: Array[String]){

    val options = getOptions();

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
    val vd2 = LsFileSet.create(vd.name, vd, depth, reverse, patterns);

    var map = ObjectBank.getFiles;

    val s = label match {
      case Some(l) => ObjectBank.putFile(l, vd2, map);
      case None => ObjectBank.putFile(vd2, map);
    }
    val vd2key = s._1;
    map = s._2;
    if(!vd2.isSingleFile){
      println("[%s]".format(vd2key));
    }

    val n: Int = if(cli.hasOption("a")){
      -1;
    } else if(cli.hasOption("n")){
      cli.getOptionValue("n").toInt;
    } else {
      20;
    }

    def printFile(f: File){
      val s = ObjectBank.putFile(f, map);
      printFileInfo(s._1, f, printTimeFormat, printFormat)
      map = s._2;
    }
    if(n < 0){
      vd2.foreach { printFile _ }
    } else {
      var i = 0;
      var l = vd2;
      var existsNext = !l.isEmpty;
      while(existsNext){
        printFile(l.head);
        i = i + 1;
        l = l.tail;
        if(i >= n){
          val s = ObjectBank.putFile(l, map);
          val k = s._1;
          map = s._2;
          println("show more: [%s]".format(k));
          existsNext = false;
        } else {
          existsNext = !l.isEmpty;
        }
      }
    }

    ObjectBank.putFiles(map);

  }

  def help(cmdName: String){
    val formatter = new HelpFormatter();
    formatter.printHelp("ls", getOptions());
  }

  private def getOptions(): Options = {
    val options = new Options();
    options.addOption("r", false, "list subdirectories recursively");
    options.addOption( {
      val op = new CliOption("R", "recursive", true,
        "list subdirectories recursively");
      op.setArgName("depth");
      op;
    } );
    options.addOption("e", "reverse", false, "list in reverse order");
    options.addOption( {
      val op = new CliOption("s", "pattern", true,
        "search by file name");
      op.setArgName("pattern");
      op;
    } );
    options.addOption( {
      val op = new CliOption("T", "time", true,
        "specify the time display format");
      op.setArgName("format");
      op;
    } );
    options.addOption( {
      val op = new CliOption("f", "format", true,
        "specify the display format");
      op.setArgName("format");
      op;
    } );
    options.addOption( {
      val op = new CliOption("l", "label", true,
        "label the list");
      op.setArgName("label");
      op;
    } );
    options.addOption("a", false, "list all files");
    options.addOption( {
      val op = new CliOption("n", true,
        "list the first n files");
      op.setArgName("n");
      op;
    } );
    options;
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
      case None => "%1$s %2$s  [%3$s]";
    }
    println(f.format(t, file, key));
  }

  case class LsFileSet(name: String,
    files: FileSet, depth: Int, reverseOrder: Boolean,
    next: FileSet) extends FileSet {

    override def isEmpty: Boolean = if(!files.isEmpty){
      false;
    } else {
      next.isEmpty;
    }

    @transient private var _headTail: (File, FileSet) = null;
    private def headTail: (File, FileSet) = {
      if(_headTail==null){
        synchronized {
          if(_headTail==null){
            _headTail = $headTail;
          }
        }
      }
      _headTail;
    }
    private def $headTail: (File, FileSet) = {
      if(files.isEmpty){
        (next.head, next.tail);
      } else {
        val f = files.head;
        val tail = if(depth==0){
          LsFileSet(name, files.tail, depth, reverseOrder, next);
        } else {
          LsFileSet(name, FileSet.DirFileSet(f, reverseOrder),
            depth - 1, reverseOrder, LsFileSet(name, files.tail, depth,
            reverseOrder, next));
        }
        (f, tail);
      }
    }

    override def head: File = headTail._1;

    override def tail: FileSet = headTail._2;

  }

  object LsFileSet {

    def create(name: String, vd: FileSet,
      depth: Int, reverseOrder: Boolean,
      pattern: Option[String]): FileSet = {

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

      (if(depth==0 && !reverseOrder && pattern==None){
        vd;
      } else {
        LsFileSet(name, vd, depth, reverseOrder, FileSet.empty);
      }).filter(cond(_));

    }

  }

}
