package com.github.tomxiong.spellchecker.dictionary;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class SpellSuggesterTest {

  @Test
  public void computeDamerauLevenshteinDistance() {
    Map<Integer, Set<String>> words = new LinkedHashMap<>();
    words.put(4, new HashSet<>(Arrays.asList("word", "work", "mark", "wood", "test")));
    SpellSuggester suggester = SpellSuggester.getInstance(words);
    suggester.setWords(words);
    Map<Integer, List<String>> editDistance = suggester.suggest("wirk", 3, 1);
    assertEquals(editDistance.toString(), "{1=[work]}", editDistance.toString());

    editDistance = suggester.suggest("wrod", 3, 2);
    assertEquals(editDistance.toString(),"{2=[work], 1=[wood, word]}", editDistance.toString());

    editDistance = suggester.suggest("wrdo", 3, 2);
    assertEquals(editDistance.toString(), "{2=[wood, word]}", editDistance.toString());

    suggester.setStringDistance(new DamerauLevenshteinDistance());
    editDistance = suggester.suggest("wirk", 3, 2);
    assertEquals(editDistance.toString(), "{1=[work], 2=[word, mark]}", editDistance.toString());

    editDistance = suggester.suggest("wrod", 3, 2);
    assertEquals(editDistance.toString(),"{2=[work], 1=[wood, word]}", editDistance.toString());

    editDistance = suggester.suggest("wrdo", 3, 2);
    assertEquals(editDistance.toString(), "{2=[wood, word]}", editDistance.toString());
  }
}