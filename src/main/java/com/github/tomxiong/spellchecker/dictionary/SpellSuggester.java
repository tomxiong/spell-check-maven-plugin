package com.github.tomxiong.spellchecker.dictionary;

import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpellSuggester {

  private static SpellSuggester suggester;
  private Map<Integer, Set<String>> words;
  private StringDistance stringDistance = new DamerauLevenshteinDistance();
  protected static SpellSuggester getInstance(Map<Integer, Set<String>> words) {
    if (suggester == null) {
      suggester = new SpellSuggester(words);
    }
    return suggester;
  }

  private SpellSuggester(Map<Integer, Set<String>> words) {
    this.words = words;
  }

  public void setStringDistance(StringDistance stringDistance) {
    this.stringDistance = stringDistance;
  }

  public void setWords(Map<Integer, Set<String>> words) {
    this.words = words;
  }

  public Map<Integer, List<String>> suggest(String word, int tol, int maxEditDistance) {
    Map<Integer, List<String>> result = new LinkedHashMap<>();

    //for (int i = 0; i <= maxEditDistance; i++) {
    //  result.put(i, new ArrayList<>());
    //}
    for (int i = 0; i <= tol; i++) {
      if (0 != i) {
        doSuggest(word, words,word.length() + i, result, maxEditDistance);
        doSuggest(word, words, word.length() - i, result, maxEditDistance);
      } else {
        doSuggest(word, words, word.length(), result, maxEditDistance);
      }
    }
    return result;
  }

  /**
   * This method search similar words with given length <code>len</code>.
   *
   * @param word   word to search.
   * @param len    from words of length <code>len</code> to search
   * @param result suggest result.
   * @param maxEditDistance The max edit for distance of the word
   */
  private void doSuggest(String word, Map<Integer, Set<String>> dict, int len, Map<Integer, List<String>> result, int maxEditDistance) {
    if (len < 1) {
      return;
    }
    Set<String> wordsByLen = dict.get(len);
    if (wordsByLen != null) {
      for (String wordByLen : wordsByLen) {
        //int editDistance = computeDamerauLevenshteinDistance(word, candidateWord);
        int editDistance = Math
            .toIntExact(Math.round(stringDistance.getDistance(word, wordByLen, maxEditDistance)));
        if (editDistance > 0 && editDistance <= maxEditDistance) {
          List candidateWords = result.get(editDistance);
          if (candidateWords == null) {
            candidateWords = new LinkedList();
            result.put(editDistance, candidateWords);
          }
          candidateWords.add(wordByLen);
        }
      }
    }
  }


}
