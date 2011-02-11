
import hydrocul.kametest.{ Test => KTest };

object Test {

  def main(args: Array[String]){

    hydrocul.kametools.SampleTest.test();
    hydrocul.kametools.ObjectBankTest.test();
    hydrocul.kametools.HelpAppTest.test();
    hydrocul.kametools.print.PrintAppTest.test();
    hydrocul.kametools.time.NowAppTest.test();

    println("Success: %d / %d".format(KTest.getSuccessCount, KTest.getTotalCount));

    if(KTest.errorExists){
      System.exit(1);
    }
    System.exit(0);

  }

}
