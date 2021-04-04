package com.github.tomxiong.spellchecker.checker;

import static java.util.Objects.isNull;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.maven.plugin.logging.Log;

public abstract class AbstractSpellChecker {

  protected Dictionary dictionary;
  protected Log logger;

  private Map<String, Set<String>> needCheck = new HashMap<>();

  private Set<String> allowWord = new HashSet<>();

  protected AbstractSpellChecker(Dictionary dict, Log logger) {
    this.logger = logger;
    this.dictionary = dict;
  }

  public Dictionary getDictionary() {
    return this.dictionary;
  }

  public void setDictionary(Dictionary dictionary) {
    this.dictionary = dictionary;
  }

  protected Collection<String> checkWordsAndSuggest(String word) {
    if (isAlpha(word)) {
      if (!dictionary.isWord(word.toLowerCase()) && !isCustomWord(word.toLowerCase())) {
        return dictionary.suggest(word.toLowerCase());
      }
      else {
        return Collections.emptyList();
      }
    }
    return Collections.emptyList();
  }

  public boolean isAlpha(String str) {
    if(str==null) return false;
    return str.matches("[a-zA-Z]+");
  }

  protected boolean isCustomWord(String word) {
    return allowWord.contains(word);
  }

  public abstract List<String> tokenize(String line);

  protected Set<String> findMatchedWords(String newline) {
    String[] splitWords = newline.split(",|\\s+|\\.|:");
    Set<String> words = new LinkedHashSet<>(Arrays.asList(splitWords));
    words.remove("");
    return words;
  }

  protected boolean isValidWord(String extractedWord) {
    if (extractedWord == null || extractedWord.trim().isEmpty()) {
      return false;
    }
    if (extractedWord.contains("_")) {
      return false;
    }
    // The upper case character means short name or specific name.
    if (extractedWord.equals(extractedWord.toUpperCase())) {
      return false;
    }
    int len = extractedWord.length();
    for (int i = 0; i < len; i++) {
      // checks whether the character is neither a letter nor a digit
      // if it is neither a letter, nor a digit then it will return false
      if ((Character.isDigit(extractedWord.charAt(i)))) {
        return false;
      }
    }
    return true;
  }

  public boolean isCompoundWord(String extractedWord) {
    if (extractedWord.equals(extractedWord.toLowerCase())) {
      return false;
    }
    return !extractedWord.equals(extractedWord.toUpperCase()) || extractedWord.contains("_");
  }

  public List<String> parseCompoundWord(String compoundWord) {
    List<String> words = new LinkedList<>();
    //handle UPPER_CASE_NAMES
    if (compoundWord.equals(compoundWord.toUpperCase())) {
      words.addAll(Arrays.asList(compoundWord.split("_")));
      return words;
    }

    StringBuilder builder = new StringBuilder(1024);
    builder.delete(0, builder.length());
    final int len = compoundWord.length();
    int i = 0;
    int j = 0;
    char c;
    for (; i < len; i++) {
      if (i == len - 1) {
        words.add(compoundWord.substring(j));
        break;
      }

      c = compoundWord.charAt(i);
      if ((c >= 'A' && c <= 'Z') && (i > j)) {
          words.add(compoundWord.substring(j, i));
          j = i;
      }
    }
    return words;
  }

  public Log getLogger() {
    return logger;
  }

  public void setLogger(Log logger) {
    this.logger = logger;
  }

  protected Map<String, Collection<String>> checkLine(String line) {
    List<String> checkedWords = tokenize(line);
    Map<String, Collection<String>> results = new LinkedHashMap<>(checkedWords.size());
    for (String word : checkedWords) {
      try {
        Collection<String> suggestions = checkWordsAndSuggest(word);
        if (!suggestions.isEmpty()) {
          results.put(word, suggestions);
        }
      } catch (NoSuchElementException e) {
        getLogger().error("Failed to check line [" + line + "]'s word " + word, e);
      } catch (Exception e) {
        getLogger().error("Failed to check line [" + line + "]'s word " + word, e);
        throw e;
      }
    }
    return results;
  }
  public Collection<CheckResult> check(File file, boolean onlyList) {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
      List<CheckResult> lineResults = new LinkedList<>();
      String line = bufferedReader.readLine();
      int lineNum = 0;
      while (null != line) {
        lineNum++;
        if (!line.trim().isEmpty() && isValidLine(line.trim())) {
          if (onlyList) {
            lineResults.add(new CheckResult(lineNum, line, null));
          }
          else {
            Map<String, Collection<String>> lineMap = checkLine(line);
            if (!lineMap.isEmpty()) {
              lineResults.add(new CheckResult(lineNum, line, lineMap));
            }
          }
        }
        line = bufferedReader.readLine();
      }
      return lineResults;
    } catch (Exception e) {
      getLogger().error("Failed to check file:" + file.getName(), e);
    }
    return Collections.emptyList();
  }

  protected abstract boolean isValidLine(String trim);

  public void addCustomCheckList(Map<String, String> xmlElementMap) {
    for (Map.Entry<String, String> entrySet : xmlElementMap.entrySet()) {
      String value = entrySet.getValue();
      if (!isNull(value) && !value.isEmpty()) {
        needCheck.put(entrySet.getKey(), new HashSet<>(Arrays.asList(value.split(","))));
      }
    }
  }

  public Map<String, Set<String>> getCheckListMap() {
    return needCheck;
  }

  public void addAllowWord(Set<String> allowWord) {
    this.allowWord.addAll(allowWord);
  }

  public void setUseSymSpellCheck(boolean use) {
    this.dictionary.setUseSymSpellCheck(use);
  }
}
