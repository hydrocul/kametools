package hydrocul.kametools;

import java.io.File;

import hydrocul.kametest.Test;

object ObjectBankTest {

  def test(){

    ObjectBank.default.put("test_1", Some(List(3, 4, 5)));
    Test.assertEquals("", Some(List(3, 4, 5)), ObjectBank.default.get("test_1"));

    Test.assertEquals("", None, ObjectBank.default.get("test_2"));

    Test.assertEquals("", List(3, 4, 5),
      ObjectBank.default.getOrElse("test_1", List(5, 6, 7)));

    Test.assertEquals("", "abc",
      ObjectBank.default.getOrElse("test_1", "abc"));

    Test.assertEquals("", List(5, 6, 7),
      ObjectBank.default.getOrElse("test_2", List(5, 6, 7)));

    Test.assertEquals("", Some("test_1"),
      ObjectBank.default.getNameByValue(List(3, 4, 5)));

    Test.assertEquals("", None,
      ObjectBank.default.getNameByValue(List(5, 6, 7)));

    val name1 = ObjectBank.default.put(Range(123, 125));
    Test.assertEquals("", Some(Range(123, 125)), ObjectBank.default.get(name1));

    Test.assertEquals("", Some(name1),
      ObjectBank.default.getNameByValue(Range(123, 125)));

    ObjectBank.default.put("test_1", None);
    Test.assertEquals("", None, ObjectBank.default.get("test_1"));

    ObjectBank.default.remove(name1);
    Test.assertEquals("", None, ObjectBank.default.get(name1));

  }

}
