package hydrocul.kametools.ls;

import java.io.File;

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
      val op = new CliOption("R", "recursive", true, "list subdirectories recursively");
      op.setArgName("depth");
      op;
    } );
    options.addOption("reverse", false, "reverse order while sorting");
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

    val reverse = cli.hasOption("reverse");

    val vd = VirtualDirectory.getArgFiles(cli.getArgs, Some("./"),
      false, true, env);
    val vd2 = LsVirtualDirectory(vd, depth, reverse);

    val list: Stream[File] = vd2.getList;

    var map = env.objectBank.getFiles;

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
    depth: Int, reverseOrder: Boolean) extends VirtualDirectory {

    override def getName = vd.getName +
      (if(depth==0) "" else " recursive (%d)".format(depth));

    override def getList = extractDir();

    override def getChild(path: String) = VirtualDirectory.empty;

    override def getChildren = this;

    private def extractDir(): Stream[File] = {
      val l = if(reverseOrder) vd.getList.reverse else vd.getList;
      extractDir(l, depth, reverseOrder, Stream.empty);
    }

    private def extractDir(files: Stream[File], depth: Int, reverseOrder: Boolean,
      next: => Stream[File]): Stream[File] = {

      if(files.isEmpty){
        next;
      } else {
        val f = files.head;
        if(depth == 0){
          Stream.cons(f, extractDir(files.tail, depth, reverseOrder, next));
        } else {
          Stream.cons(f, {
            val l: Stream[File] = VirtualDirectory.
              OneFileVirtualDirectory(f, true).getList;
            val l2 = if(reverseOrder) l.reverse else l;
            extractDir(l2, depth - 1, reverseOrder,
              extractDir(files.tail, depth, reverseOrder, next));
          });
        }
      }
    }

  }

}
