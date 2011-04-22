package hydrocul.kametools;

import java.io.File;

case class SvnCpApp(src: File, dst: File, vtype: SvnCpApp.VersionControlType) extends App {

  override def exec(env: App.Env){
    SvnCpApp.copyRecursively(src, dst, vtype, env);
  }

}

object SvnCpApp {

  def create(args: List[String]): App = create(None, args);

  private def create(app: Option[SvnCpApp], args: List[String]): App = {
    args match {
      case "help" :: tail => HelpApp;
      case src :: dst :: Nil => {
        val srcFile = new File(src);
        val dstFile = new File(dst);
        SvnCpApp(srcFile, dstFile, SvnType);
      }
      case _ => throw new Exception("Unknown arguments: " + args);
    }
  }

  object HelpApp extends App {

    override def exec(env: App.Env){
      val msg = List(
        "This program is implemented in SvnCpApp.scala."
      );
      msg.foreach(env.out.println(_));
    }

  }

  sealed abstract class VersionControlType;
  object NoneType extends VersionControlType;
  object SvnType extends VersionControlType;

  def copyRecursively(src: File, dst: File, vtype: VersionControlType, env: App.Env){
    if(!src.exists){
      if(!dst.exists){
        // nothing
      } else if(dst.isFile){
        env.shellScriptWriter.println("rm " +
          App.escapeFilePath(dst.getAbsolutePath));
        if(vtype==SvnType){
          env.shellScriptWriter.println("svn rm " +
            App.escapeFilePath(dst.getAbsolutePath));
        }
      } else if(dst.isDirectory){
        env.shellScriptWriter.println("rm -r " +
          App.escapeFilePath(dst.getAbsolutePath));
        if(vtype==SvnType){
          env.shellScriptWriter.println("svn rm " +
            App.escapeFilePath(dst.getAbsolutePath));
        }
      } else {
        throw new Exception("Unknown file type: " + dst);
      }
    } else if(src.isFile){
      if(!dst.exists){
        env.shellScriptWriter.println("cp " +
          App.escapeFilePath(src.getAbsolutePath) + " " +
          App.escapeFilePath(dst.getAbsolutePath));
        if(vtype==SvnType){
          env.shellScriptWriter.println("svn add " +
            App.escapeFilePath(dst.getAbsolutePath));
        }
      } else if(dst.isFile){
        env.shellScriptWriter.println("cp " +
          App.escapeFilePath(src.getAbsolutePath) + " " +
          App.escapeFilePath(dst.getAbsolutePath));
      } else if(dst.isDirectory){
        env.shellScriptWriter.println("rm -r " +
          App.escapeFilePath(dst.getAbsolutePath));
        env.shellScriptWriter.println("cp " +
          App.escapeFilePath(src.getAbsolutePath) + " " +
          App.escapeFilePath(dst.getAbsolutePath));
      } else {
        throw new Exception("Unknown file type: " + dst);
      }
    } else if(src.isDirectory){
      if(!dst.exists){
        env.shellScriptWriter.println("cp -r " +
          App.escapeFilePath(src.getAbsolutePath) + " " +
          App.escapeFilePath(dst.getAbsolutePath));
        if(vtype==SvnType){
          env.shellScriptWriter.println("svn add " +
            App.escapeFilePath(dst.getAbsolutePath));
        }
      } else if(dst.isFile){
        env.shellScriptWriter.println("rm " +
          App.escapeFilePath(dst.getAbsolutePath));
        env.shellScriptWriter.println("cp -r " +
          App.escapeFilePath(src.getAbsolutePath) + " " +
          App.escapeFilePath(dst.getAbsolutePath));
      } else if(dst.isDirectory){
        val srcChildren = src.list.toList;
        val dstChildren = dst.list.toList;
        val srcChildren2 = if(vtype==SvnType){
          srcChildren.filter(_ != ".svn");
        } else {
          srcChildren;
        }
        val dstChildren2 = if(vtype==SvnType){
          dstChildren.filter(_ != ".svn");
        } else {
          dstChildren;
        }
        srcChildren2.foreach( s => copyRecursively(new File(src, s), new File(dst, s), vtype, env) );
        dstChildren2.filter( d => !srcChildren2.contains(d) ).
          foreach( d => copyRecursively(new File(src, d), new File(dst, d), vtype, env) );
      } else {
        throw new Exception("Unknown file type: " + dst);
      }
    } else {
      throw new Exception("Unknown file type: " + src);
    }
  }

}

