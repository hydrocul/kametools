package hydrocul.kametools;

import java.io.File;

import hydrocul.kametest.Test;

object ObjectBankTest {

  def test(){

    ObjectBank.default.put("test_1", List(3, 4, 5));
    Test.assertEquals("", Some(List(3, 4, 5)), ObjectBank.default.get("test_1"));

    Test.assertEquals("", None, ObjectBank.default.get("test_2"));

    Test.assertEquals("", List(3, 4, 5),
      ObjectBank.default.getOrElse("test_1", List(5, 6, 7)));

    Test.assertEquals("", List(5, 6, 7),
      ObjectBank.default.getOrElse("test_2", List(5, 6, 7)));

    Test.assertEquals("", Some("test_1"),
      ObjectBank.default.getNameByValue(List(3, 4, 5)));

    Test.assertEquals("", None,
      ObjectBank.default.getNameByValue(List(5, 6, 7)));

    val name1 = ObjectBank.default.put(123);
    Test.assertEquals("", Some(123), ObjectBank.default.get(name1));

    Test.assertEquals("", Some(name1),
      ObjectBank.default.getNameByValue(123));

    ObjectBank.default.remove(name1);
    Test.assertEquals("", None,
      ObjectBank.default.getNameByValue(123));

  }

}
