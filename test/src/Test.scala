
import hydrocul.kametest.{ Test => KTest };

object Test {

  def main(args: Array[String]){

    KTest.test();
    hydrocul.kametools.ObjectBankTest.test();
//    hydrocul.kametools.FileSetTest.test();
//    hydrocul.kametools.PrintAppTest.test();
//    hydrocul.kametools.LsAppTest.test();

    println("Success: %d / %d".format(KTest.getSuccessCount, KTest.getTotalCount));

    if(KTest.errorExists){
      System.exit(1);
    }
    System.exit(0);

  }

}
