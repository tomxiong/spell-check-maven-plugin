package spellchecker.checker;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import spellchecker.dictionary.Dictionary;

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
    Collection<CheckResult> result = checker
        .check(new File("src/test/resources/xml/config.xml"), false);
    assertEquals(result.toString(), 1, result.size());
    CheckResult checkResult = (CheckResult) result.iterator().next();
    assertEquals(checkResult.suggestions.toString(), 1, checkResult.suggestions.size());


    checker.setUseSymSpellCheck(false);
    checker.getDictionary().clearCache();
    result = checker
        .check(new File("src/test/resources/xml/config.xml"), false);
    assertEquals(result.toString(), 1, result.size());
    checkResult = (CheckResult) result.iterator().next();
    assertEquals(checkResult.suggestions.toString(), 1, checkResult.suggestions.size());

  }

}
