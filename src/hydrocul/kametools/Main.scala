package hydrocul.kametools;

object Main {

  def main(args: Array[String]){
    if(args.size == 0){
      printHelp();
    } else {
      args(0) match {
        case "now" =>
          now.Now.main(args.drop(1));
        case cmd =>
          println("Unknown command: " + cmd);
      }
    }
  }

  def printHelp(){
    println("kametools now");
  }

}
