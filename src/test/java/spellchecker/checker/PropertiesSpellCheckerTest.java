package spellchecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import spellchecker.dictionary.Dictionary;

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
    Collection<CheckResult> results = checker.check(file,false);
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
    assertEquals(wordEntry.getValue().toString(), 10, ((Collection) wordEntry.getValue()).size());


    checker.setUseSymSpellCheck(true);
    checker.getDictionary().clearCache();
    Collection<CheckResult> results1 = checker.check(file,false);
    assertEquals(results1.toString(), 2, results1.size());

    iter = results.iterator();
    checkResult = (CheckResult) iter.next();
    assertEquals(checkResult.toString(), 6, checkResult.lineNum);

    checkResult = (CheckResult) iter.next();
    assertEquals(checkResult.toString(), 7, checkResult.lineNum);
    crIter = checkResult.suggestions.entrySet().iterator();
    wordEntry = (Entry) crIter.next();
    assertEquals(wordEntry.toString(), "uery", wordEntry.getKey());
    assertEquals(wordEntry.getValue().toString(), 10, ((Collection) wordEntry.getValue()).size());

    wordEntry = (Entry) crIter.next();
    assertEquals(wordEntry.toString(), "databse", wordEntry.getKey());
    assertEquals(wordEntry.getValue().toString(), 10, ((Collection) wordEntry.getValue()).size());

  }
}