package hydrocul.kametools.ls;

import java.io.File;

import hydrocul.kametest.Test;

object LsTest {

  def test(){

    val testDir = "./tmp/testDir";

    (new File(testDir)).mkdirs();

    (new File(testDir + "/a.txt")).createNewFile();

    // TODO 作りかけ

    Test.assertEquals("sample", "sampleText", "sampleText");
  }

}
