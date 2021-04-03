package com.github.tomxiong.spellchecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class PropertiesSpellCheckerTest {

  @Test
  public void testTokenize() {
    PropertiesSpellChecker checker = new PropertiesSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
    assertEquals(
        "[{0}, processor, of, {1}, collection, is, not, able, to, run, Reason, the, sql, handle, SQL, for, which, text, has, be, retrieved, missing]",
        checker.tokenize(
            "test.MissingTopSQLs={0} processor of {1} collection  is not able to run. Reason :  the sql handle of the SQL for which text has to be retrieved is missing .")
            .toString());
  }

  @Test
  public void testCheckWord() {
    PropertiesSpellChecker checker = new PropertiesSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
    //String result = checker.checkLine("dtcstats=DTC Service Status. The disributed Transaction Coordinator (DTC) service is installed but not running.", 22);
    //assertEquals(result, "", result);
    checker.setUseSymSpellCheck(true);
    Collection<String> result = checker.checkWordsAndSuggest("disributed");
    assertEquals(result.toString(), 4, result.size());

    result = checker.checkWordsAndSuggest("initialize");
    assertEquals(result.toString(), 0, result.size());
  }

  @Test
  public void testCheckFile() {
    PropertiesSpellChecker checker = new PropertiesSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
    checker.setUseSymSpellCheck(false);
    checker.getDictionary().clearCache();
    checker.addAllowWord(new HashSet<>(Collections.singletonList("fogam")));
    File file = new File("target/test-classes/properties/1.properties");
    assertTrue(file.toString(), file.exists());
    /*Collection<CheckResult> results = checker.check(file,false);
    assertEquals(results.toString(), 2, results.size());
    Iterator iter = results.iterator();
    CheckResult checkResult = (CheckResult) iter.next();
    assertEquals(checkResult.toString(), 6, checkResult.lineNum);

    checkResult = (CheckResult) iter.next();
    assertEquals(checkResult.toString(), 7, checkResult.lineNum);
    Iterator crIter = checkResult.suggestions.entrySet().iterator();
    Entry wordEntry = (Entry) crIter.next();
    assertEquals(wordEntry.toString(), "uery", wordEntry.getKey());
    assertEquals(wordEntry.getValue().toString(), 10, ((Collection) wordEntry.getValue()).size());

    wordEntry = (Entry) crIter.next();
    assertEquals(wordEntry.toString(), "databse", wordEntry.getKey());
    assertEquals(wordEntry.getValue().toString(), 9, ((Collection) wordEntry.getValue()).size());*/


    checker.setUseSymSpellCheck(true);
    checker.getDictionary().clearCache();
    Collection<CheckResult> results1 = checker.check(file,false);
    assertEquals(results1.toString(), 2, results1.size());

    Iterator iter1 = results1.iterator();
    CheckResult checkResult1 = (CheckResult) iter1.next();
    assertEquals(checkResult1.toString(), 6, checkResult1.lineNum);

    checkResult1 = (CheckResult) iter1.next();
    assertEquals(checkResult1.toString(), 7, checkResult1.lineNum);
    Iterator crIter1 = checkResult1.suggestions.entrySet().iterator();
    Entry wordEntry1 = (Entry) crIter1.next();
    assertEquals(wordEntry1.toString(), "uery", wordEntry1.getKey());
    assertEquals(wordEntry1.getValue().toString(), 10, ((Collection) wordEntry1.getValue()).size());

    wordEntry1 = (Entry) crIter1.next();
    assertEquals(wordEntry1.toString(), "databse", wordEntry1.getKey());
    assertEquals(wordEntry1.getValue().toString(), 5, ((Collection) wordEntry1.getValue()).size());

  }
}