package com.github.tomxiong.spellchecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class XmlSpellCheckerTest {

  @Test
  public void testTokenize() {
    SystemStreamLog logger = new SystemStreamLog();
    XmlSpellChecker checker = new XmlSpellChecker(Dictionary.getInstance(), logger);
    assertEquals("[Blockers, Connections, Baseline, Deviation, @messageWarning]", checker
        .tokenize(
            "Blockers Connections Baseline Deviation. @messageWarning")
        .toString());
  }

  @Test
  public void testisValidLine() {
    SystemStreamLog logger = new SystemStreamLog();
    XmlSpellChecker checker = new XmlSpellChecker(Dictionary.getInstance(), logger);
    assertTrue(checker.isValidLine("test"));
    assertTrue(checker.isValidLine("Database test"));
    assertTrue(checker.isValidLine("Database Unresponsive. "));
    assertTrue(checker.isValidLine("Database Unresponsive. Azure SQL Database @server/@database is not running (down) or not responding, and displays the following error message: \"@error_msg\""));
  }

  @Test
  public void testCheckFile() {
    SystemStreamLog logger = new SystemStreamLog();
    XmlSpellChecker checker = new XmlSpellChecker(Dictionary.getInstance(), logger);
    checker.setUseSymSpellCheck(true);
    checker.getDictionary().clearCache();
    checker.addCustomCheckList(new LinkedHashMap<String, String>() {{
      put("simple-rule", "name,comments,help");
      put("severity-family", "name,comments,help");
      put("message", "CDATA");
    }});
    File file = new File("target/test-classes/xml/config.xml");
    assertTrue(file.toString(), file.exists());
    Collection<CheckResult> result = checker.check(file,false);
    assertEquals(result.toString(), 1, result.size());
    CheckResult checkResult = result.iterator().next();
    assertEquals(checkResult.suggestions.toString(), 1, checkResult.suggestions.size());


    checker.setUseSymSpellCheck(false);
    checker.getDictionary().clearCache();
    result.clear();
    result = checker.check(file, false);
    /*assertEquals(result.toString(), 1, result.size());
    checkResult = result.iterator().next();
    assertEquals(checkResult.suggestions.toString(), 1, checkResult.suggestions.size());*/

  }

}
