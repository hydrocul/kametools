package hydrocul.kametools;

trait ObjectBank {

  import ObjectBank._;

  def get: Map[String, Field];

  def save(map: Map[String, Field]);

}

object ObjectBank {

  case class Field(typeName: String, value: AnyRef){

    def get[A]: A = value.asInstanceOf[A];

    def getOrElse[A](defaultValue: =>A): A = value match {
      case A => value.asInstanceOf[A];
      case _ => defaultValue;
    }

  }

  def getBank(fileName: String): ObjectBank = new ObjectBank(){

    private val map: Option[Map[String, Field]] = None;

    import scala.actors.Actor.actor;

    private val ioActor = actor {
      loop {
        react {
          case "load" => load();
          case "save" => save();
        }
      }
    }

    def get: Map[String, Field];

  }

}
