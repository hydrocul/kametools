package hydrocul.kametest;

import java.io.PrintWriter;
import java.io.StringWriter;

object Test {

  private var _successCount: Int = 0;
  private var _errorCount: Int = 0;
  private var _errorExists: Boolean = false;

  def errorExists: Boolean = _errorExists;

  def getSuccessCount = _successCount;

  def getErrorCount = _errorCount;

  def getTotalCount = _successCount + _errorCount;

  def assertEquals[A](name: String, expected: A, actual: A){
    if(actual==expected){
      _successCount = _successCount + 1;
      return;
    }
    val msg = "[ERROR] %s: expedted: %s, actual: %s %s".format(name, expected, actual, {
      val sw = new StringWriter();
      val pw = new PrintWriter(sw);
      (new AssertionError()).printStackTrace(pw);
      sw.toString.split("\n")(2).trim;
    });
    println(msg);
    synchronized {
      _errorCount = _errorCount + 1;
      _errorExists = true;
    }
  }

}
