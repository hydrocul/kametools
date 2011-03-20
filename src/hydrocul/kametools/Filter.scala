package hydrocul.kametools;

import java.io.File;

trait Filter {

  def apply(arg: String): AnyRef;

  def isDefinedAt(arg: String): Boolean;

  def help: Iterable[Filter.HelpLine];

}

trait FilterContainer {

  def filter(obj: AnyRef): Option[Filter];

}

object Filter {

  case class HelpLine(arg: String, explanation: String);

  def next(obj: AnyRef, arg: String): AnyRef = {
    filters.foreach { filter =>
      filter.filter(obj) match {
        case Some(filter) =>
          if(filter.isDefinedAt(arg)){
            return filter(arg);
          }
        case _ => ;
      }
    }
    throw new Exception("Unknown argument: " + arg);
  }

  def finish(obj: AnyRef){
    obj match {
      case app: App => app.exec();
      case StartApp => throw new Exception("No argument");
      case obj => App.toApp(obj).exec();
    }
  }

  object StartApp extends java.io.Serializable;

  def create(f: Function[AnyRef, Option[Filter]]) = new FilterContainer(){

    override def filter(obj: AnyRef) = f(obj);

  }

  def create(pf: PartialFunction[String, AnyRef], help_ : Iterable[HelpLine]) = new Filter(){

    override def apply(arg: String): AnyRef = pf.apply(arg);

    override def isDefinedAt(arg: String): Boolean = pf.isDefinedAt(arg);

    override def help: Iterable[HelpLine] = help_;

  }

  private lazy val filters: List[FilterContainer] =
    StartAppFilter ::
//    FileSet.filter ::
    StandardFilter ::
    ObjectBankFilter ::
    ArgumentExpectedFilter :: Nil;

  private val StartAppFilter = create({ obj =>
    obj match {
      case StartApp => Some(create({
        case arg if(arg.startsWith("./")) =>
          (new File(arg.substring(2))).getAbsoluteFile;
        case arg if(arg.startsWith("../") || arg.startsWith("/")) =>
          (new File(arg)).getAbsoluteFile;
      }, Array(
        HelpLine("<path>", "file")
      )));
      case _ => None;
    }
  });

  private val StandardFilter = create({ obj =>
    obj match {
      case obj => Some(create({
        case "class" => obj.getClass();
      }, Array(
        HelpLine("class", "class")
      )));
    }
  });

  private val ObjectBankFilter = create({ obj =>
    obj match {
      case StartApp => Some(new Filter {

        override def apply(arg: String) = ObjectBank.default.get(arg) match {
          case Some(o) => o;
          case None => throw new Exception("Unknown object: " + arg);
        }

        override def isDefinedAt(arg: String) = ObjectBank.default.isDefinedAt(arg);

        override def help: Iterable[HelpLine] = Array(HelpLine("<key>", "object"));

      });
      case _ => None;
    }
  });

  private val ArgumentExpectedFilter = create({ obj =>
    obj match {
      case obj: Filter => Some(obj);
      case _ => None;
    }
  });

}
