package hydrocul.kametest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;

object Test {

  private var _successCount: Int = 0;
  private var _errorCount: Int = 0;
  private var _errorExists: Boolean = false;

  def errorExists: Boolean = _errorExists;

  def getSuccessCount = _successCount;

  def getErrorCount = _errorCount;

  def getTotalCount = _successCount + _errorCount;

  def assertEquals[A](name: String, expected: A, actual: A){

    val r: Boolean = checkEquals(expected, actual);

    if(r){
      _successCount = _successCount + 1;
      return;
    }

    val msg = "[ERROR] %s: expedted: %s, actual: %s %s".
      format(name, expected, actual, {
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

  private def checkEquals[A](expected: A, actual: A): Boolean = {
    if(actual==expected){
      true;
    } else {
      (expected, actual) match {
        case (p: StringPattern, actual: String) => p.matches(actual);
        case _ => false;
      }
    }
  }

  case class StringPattern(regex: String){

    def matches(actual: String): Boolean = {
      val p = Pattern.compile(regex);
      val m = p.matcher(actual);
      m.matches();
    }

  }

  def test(){

    assertEquals("", "abc", "abc");
    assertEquals("", StringPattern("ab."), "abc");

  }

}
