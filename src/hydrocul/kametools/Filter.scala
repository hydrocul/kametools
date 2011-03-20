package hydrocul.kametools;

import java.io.File;

trait Filter {

  def apply(arg: String, env: App.Env): AnyRef;

  def isDefinedAt(arg: String): Boolean;

  def finish(env: App.Env): Option[AnyRef];

  def help: Iterable[Filter.HelpLine];

}

trait FilterContainer {

  def filter(obj: AnyRef): Option[Filter];

}

object Filter {

  case class HelpLine(arg: String, explanation: String);

  def next(obj: AnyRef, arg: String, env: App.Env): AnyRef = {
    filters.foreach { filter =>
      filter.filter match {
        case Some(filter) =>
          if(filter.isDefinedAt(arg)){
            return filter(arg, env);
          }
        case _ => ;
      }
    }
    throw new Exception("Unknown argument: " + arg);
  }

  def finish(obj: AnyRef): AnyRef = {
    filters.foreach { filter =>
      filter.filter match {
        case Some(filter) =>
          return filter.finish(env);
        case _ => ;
      }
    }
  }

  private lazy val filters: List[Filter] =
    StartAppFilter ::
//    FileSet.filter ::
    StandardFilter ::
    ObjectBankFilter :: Nil;

  import App.StartApp;

  private val StartAppFilter = create({
    case (StartApp, arg) if(arg.startsWith("./")) =>
      (new File(arg.substring(2))).getAbsoluteFile;
    case (StartApp, arg) if(arg.startsWith("../") || arg.startsWith("/")) =>
      (new File(arg)).getAbsoluteFile;
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

    override def apply(obj: AnyRef, arg: Optoin[String]) = (obj, arg) match {
      case (StartApp, Some(arg)) => ObjectBank.default.get(arg) match {
        case Some(o) => o;
        case None => throw new Exception("Unknown object: " + arg);
      }
    }

    override def isDefinedAt(obj: AnyRef, arg: Opton[String]) = (obj, arg) match {
      case (StartApp, Some(arg)) => ObjectBank.default.isDefinedAt(arg);
      case _ => false;
    }

    override def help: Help = Help(Array(HelpLine("<key>", "object")));

  }

  private val ArgumentExpectedFilter = new Filter {

    override def apply(obj: AnyRef, arg: Optoin[String]) = (obj, arg) match {
      case (obj: ArgumentExpected, Some(arg)) => obj.apply(arg);
    }

    override def isDefinedAt(obj: AnyRef, arg: Opton[String]) = (obj, arg) match {
      case (obj: ArgumentExpected, Some(arg)) => obj.isDefinedAt(arg);
      case _ => false;
    }

    override def help: Help = 

  }

  trait ArgumentExpected {

    def apply(arg: String): AnyRef;

    def isDefinedAt(arg: String): Boolean;

    def help: HelpLine;

  }

  def createArgumentExpectedObject: AnyRef = {
  }

}
