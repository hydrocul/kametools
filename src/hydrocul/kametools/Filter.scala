package hydrocul.kametools;

import java.io.File;

trait Filter {

  def apply(obj: AnyRef, arg: String): AnyRef;

  def isDefinedAt(obj: AnyRef, arg: String): Boolean;

  def help: Filter.Help;

}

object Filter {

  def create(pf: PartialFunction[(AnyRef, String), AnyRef], helpObject: Help) = new Filter {

    override def apply(obj: AnyRef, arg: String): AnyRef = pf.apply((obj, arg));

    override def isDefinedAt(obj: AnyRef, arg: String): Boolean = pf.isDefinedAt((obj, arg));

    override def help = helpObject;

  }

  case class Help(lines: Iterable[HelpLine]);

  case class HelpLine(arg: String, explanation: String);

  def next(obj: AnyRef, arg: String): AnyRef = {
    filters.find(_.isDefinedAt(obj, arg)) match {
      case Some(filter) => filter(obj, arg);
      case None => throw new Exception("Unknown argument: " + arg);
    }
  }

  private lazy val filters: List[Filter] =
    StartAppFilter ::
    StandardFilter ::
    FileSet.filter ::
    ObjectBankFilter ::
    StartAppNoArgumentFilter :: Nil;

  import App.StartApp;

  private val StartAppFilter = create({
    case (StartApp, arg) if(arg.startsWith("./")) =>
      (new File(arg.substring(2))).getAbsoluteFile;
    case (StartApp, arg) if(arg.startsWith("../") || arg.startsWith("/")) =>
      (new File(arg)).getAbsoluteFile;
/*
    case (StartApp, "daemon") =>
      OpenApp.FileOpenReceiver;
*/
  }, Help(Array(
    HelpLine("<path>", "file")
  )));

  private val StandardFilter = create({
    case (obj, "class") =>
      obj.getClass();
  }, Help(Array(
    HelpLine("class", "class")
  )));

  private val ObjectBankFilter = new Filter {

    override def apply(obj: AnyRef, arg: String) = (obj, arg) match {
      case (StartApp, arg) => ObjectBank.default.get(arg) match {
        case Some(o) => o;
        case None => throw new Exception("Unknown object: " + arg);
      }
    }

    override def isDefinedAt(obj: AnyRef, arg: String) = (obj, arg) match {
      case (StartApp, arg) => ObjectBank.default.isDefinedAt(arg);
      case _ => false;
    }

    override def help: Help = Help(Array(HelpLine("<key>", "object")));

  }

}
