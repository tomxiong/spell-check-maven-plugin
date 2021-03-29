package spellchecker.dictionary;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.File;
import java.util.List;
import org.junit.Test;

public class DictionaryTest {

  @Test
  public void testIsWord() {
    //regressedplanexecutioncount
    Dictionary dictionary = Dictionary.getInstance();
    assertTrue(dictionary.isWord("test"));
    assertFalse(dictionary.isWord("regressedplanexecutioncount"));
  }

  @Test
  public void testIsWordWithNewDictEn80k() {
    Dictionary dictionary = Dictionary.getInstance("en-80k.txt");
    assertTrue(dictionary.isWord("test"));
    assertTrue(dictionary.isWord("topologies"));
  }

  @Test
  public void testIsWordWithNewDictWordsAlpha() {
    Dictionary dictionary = Dictionary.getInstance("words_alpha.txt");
    assertTrue(dictionary.isWord("test"));
    assertTrue(dictionary.isWord("topologies"));
  }

  @Test
  public void testIsWordWithCustomDict() {
    Dictionary dict = Dictionary.getInstance();
    assertFalse(dict.isWord("tooltips"));
    dict.loadCustomDictionary(new File("src/test/resources/customWord.txt"));
    assertTrue(dict.isWord("tooltips"));
  }

  @Test
  public void testSuggestWords() {
    Dictionary dict = Dictionary.getInstance();
    dict.setUseSymSpellCheck(false);
    dict.clearCache();
    List suggestions = dict.suggest("Hostname");
    assertEquals(suggestions.toString(), 3, suggestions.size());
    //System.out.println(suggestions.toString());

    suggestions = dict.suggest("disributed");
    assertEquals(suggestions.toString(), 7, suggestions.size());
  }

  @Test
  public void testSuggestWordsWithSymSpellCheck() {
    Dictionary dict = Dictionary.getInstance();
    dict.clearCache();
    dict.setUseSymSpellCheck(true);
    List suggestions = dict.suggest("Hostname");
    assertEquals(suggestions.toString(), 2, suggestions.size());

    suggestions = dict.suggest("disributed");
    assertEquals(suggestions.toString(), 4, suggestions.size());
  }


}