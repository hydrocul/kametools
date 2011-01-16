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

    val reverse = cli.hasOption("reverse");

    val vd = VirtualDirectory.getArgFiles(cli.getArgs, Some("./"),
      false, true, env);
    val list: Stream[File] = vd.getList;

    def extractDir(file: File, level: Int): Seq[File] = { // TODO Stream にしたほうがいい
      if(level == 0){
        Vector(file);
      } else {
        val l = file.listFiles;
        if(l == null){
          Vector(file);
        } else {
          file +: l.sortWith((a, b) => (
            if(reverse)
              VirtualDirectory.compareFileName(a.getPath, b.getPath) > 0
            else
              VirtualDirectory.compareFileName(a.getPath, b.getPath) < 0)).
            flatMap(f => extractDir(f, level - 1));
        }
      }
    }

    val list2 = if(cli.hasOption("R")){
      val depth = try {
        cli.getOptionValue("R").toInt;
      } catch {
        case e: NumberFormatException =>
          System.err.println("Illegal argument for option: R " +
            cli.getOptionValue("R"));
          return;
      }
      list.flatMap { f =>
        extractDir(f, depth);
      }
    } else if(cli.hasOption("r")){
      list.flatMap { f =>
        extractDir(f, -1);
      }
    } else {
      list;
    }

    var map = env.objectBank.getFiles;

    list2.foreach { f: File =>
      val s = env.objectBank.putFile(f, map);
      println("%s %s".format(s._1, f));
      map = s._2;
    }

    env.objectBank.putFiles(map);

  }

  def help(cmdName: String){
    // TODO
  }

}
