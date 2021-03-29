package spellchecker.checker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import spellchecker.dictionary.Dictionary;

public class JavaSpellCheckerTest {


  @Test
  public void isValidLine() {
    JavaSpellChecker checker = new JavaSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
    assertTrue(checker.isValidLine("txt= \"Blocking Current\";"));
    assertTrue(checker.isValidLine("txt=\"Blocking Current\";"));
    assertFalse(checker.isValidLine(
        "    obsValue.set(\"dipslayName\", metricWrapper.getProps()?.getDisplayName());"));
    assertFalse(checker.isValidLine(
        "baselineContext = functionHelper.createDataObject(\"dbwc_azure_azure20pi:AzurePIBasline\", \"none\", null);"));
  }

  @Test
  public void testTokenize() {
    JavaSpellChecker checker = new JavaSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
        /*
        List<String> words = checker.tokenize("private Dictionary dictionary;");
        assertEquals("[]", words.toString());

        words = checker.tokenize("throw new IllegalArgumentException(\"line to tokenize shout not be null or empty\")");
        assertEquals("[line, to, tokenize, shout, not, be, null, or, empty]", words.toString());

        words = checker.tokenize("File reportFile = new File(targetDirectory, \"spelling_check_result.txt\");");
        assertEquals("[spelling, check, result, txt]", words.toString());
        */
    List<String> words1 = checker.tokenize(
        "String result = checker.checkWordsAndSuggest(155, \"__top_of_tree_5.5.5.4__\");");
    assertEquals(3, words1.size());
  }

  @Test
  public void testCheckWordsAndSuggest() {
    JavaSpellChecker checker = new JavaSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
    //String result = checker.checkWordsAndSuggest(13, "regressedplanexecutioncount");
    //assertEquals("line 13 regressedplanexecutioncount, no any suggestions." + System.lineSeparator(), result);

    //String result = checker.checkWordsAndSuggest(89, "recommendedplanexecutioncount");
    //assertEquals("line 89 recommendedplanexecutioncount, no any suggestions." + System.lineSeparator(), result);

    Collection<String> result = checker.checkWordsAndSuggest("__top_of_tree_5.5.5.4__");
    assertEquals(Collections.EMPTY_LIST, result);


  }

  @Test
  public void testCheckLine() {
    JavaSpellChecker checker = new JavaSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
    Map resultMap = checker.checkLine("return \"The host @Hostname@ was generated.\";");
    assertEquals(resultMap.toString(), 0, resultMap.size());
    //assertEquals(resultMap.toString(), "Hostname", resultMap.keySet().iterator().next());
  }

  @Test
  public void testCheckFile() {
    JavaSpellChecker checker = new JavaSpellChecker(Dictionary.getInstance(),
        new SystemStreamLog());
    checker.setUseSymSpellCheck(true);
    Collection<CheckResult> result = checker
        .check(new File("src/test/java/spellchecker/checker/JavaSpellCheckerTest.java"), false);
    assertTrue(result.isEmpty());

    result = checker.check(new File("src/test/resources/code/test.groovy"), false);
    assertEquals(result.toString(), 1, result.size());

    result = checker.check(new File("src/test/resources/code/test1.groovy"), false);
    assertEquals(result.toString(), 0, result.size());

    /*checker.addAllowWord(new HashSet<>(Arrays.asList("hostname")));
    result = checker.check(new File("src/test/resources/code/test1.groovy"), false);
    assertEquals(result.toString(), 1, result.size());*/


  }

    /*
    @Test
    public void testPlugin() {
        Binding binding = new Binding();
        binding.setVariable("x", 10);
        binding.setVariable("language", "Groovy");

        GroovyShell shell = new GroovyShell(binding);
        Object value = shell.evaluate("println \"Welcome to $language\"; y = x * 2; z = x * 3; return x ");
        System.err.println(value +", " + value.equals(10));
        System.err.println(binding.getVariable("y") +", " + binding.getVariable("y").equals(20));
        System.err.println(binding.getVariable("z") +", " + binding.getVariable("z").equals(30));
    }

     */
}