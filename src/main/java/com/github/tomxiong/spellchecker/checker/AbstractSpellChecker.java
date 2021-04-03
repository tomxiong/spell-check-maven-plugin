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
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import org.apache.maven.plugin.logging.Log;

public abstract class AbstractSpellChecker {

  //private static Pattern WORD_PATTERN = Pattern.compile("\\w{1,}");
  protected Dictionary dictionary;
  protected Log logger;

  private Map<String, Set<String>> needCheck = new HashMap<>();

  private Set<String> allowWord = new HashSet<>();

  public AbstractSpellChecker(Dictionary dict, Log logger) {
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
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("The word is alpha : " + word);
      }
      if (!dictionary.isWord(word.toLowerCase()) && !isCustomWord(word.toLowerCase())) {
        return dictionary.suggest(word.toLowerCase());
      }
      else {
        return Collections.EMPTY_LIST;
      }
    }
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("The word is not alpha : " + word);
    }
    return Collections.EMPTY_LIST;
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
    Set<String> words = new LinkedHashSet<>();
    String[] splitWords = newline.split("\\,|\\s+|\\.|\\:");
    for(String word :splitWords) {
      words.add(word);
    }
    //return words;
    /*Matcher matcher = WORD_PATTERN.matcher(newline);
    String extractedWord;

    while (matcher.find()) {
      extractedWord = matcher.group(0);
      if (isValidWord(extractedWord)) {
        //if (!isCompoundWord(extractedWord)) {
          words.add(extractedWord);
        *//*} else {
          words.addAll(parseCompoundWord(extractedWord));
        }*//*
      }
    }*/
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
      if ((Character.isDigit(extractedWord.charAt(i)) == true)) {
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
    int i = 0, j = 0;
    char c;
    for (; i < len; i++) {
      if (i == len - 1) {
        words.add(compoundWord.substring(j));
        break;
      }

      c = compoundWord.charAt(i);
      if (c >= 'A' && c <= 'Z')
        if (i > j) {
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
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Checking word : " + word);
        }
        Collection<String> suggestions = checkWordsAndSuggest(word);
        if (!suggestions.isEmpty()) {
          if (getLogger().isDebugEnabled()) {
            getLogger().debug("Checked word : " + word);
            getLogger().debug("add suggestion : " + suggestions.toString());
          }
          results.put(word, suggestions);
        }
      } catch (NoSuchElementException e) {
        if (getLogger() != null) {
          getLogger().error("Failed to check line [" + line + "]'s word " + word, e);
        } else {
          e.printStackTrace();
        }
        //resultBuffer.append("line ").append(lineNum).append()
      } catch (IllegalArgumentException e) {
        if (getLogger() != null) {
          getLogger().error("Failed to check line [" + line + "]'s word " + word, e);
        } else {
          e.printStackTrace();
        }
        throw e;
      } catch (Exception e) {
        if (getLogger() != null) {
          getLogger().error("Failed to check line [" + line + "]'s word " + word, e);
        } else {
          e.printStackTrace();
        }
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
      if (getLogger() != null) {
        getLogger().error("Failed to check file:" + file.getName(), e);
      } else {
        e.printStackTrace();
      }
    }
    return Collections.EMPTY_LIST;
  }

  protected abstract boolean isValidLine(String trim);

  public void addCustomCheckList(LinkedHashMap<String, String> xmlElementMap) {
    for (Map.Entry<String, String> entrySet : xmlElementMap.entrySet()) {
      String value = entrySet.getValue();
      if (!isNull(value) && !value.isEmpty()) {
        needCheck.put(entrySet.getKey(), new HashSet<>(Arrays.asList(value.split(","))));
        if (getLogger() != null && getLogger().isDebugEnabled()) {
          getLogger().debug("add custom item for xml :[" + entrySet.getKey() + ":" + value + "]");
        }
      }
    }
  }

  public Map<String, Set<String>> getCheckListMap() {
    return needCheck;
  }

  public void addAllowWord(HashSet<String> allowWord) {
    this.allowWord.addAll(allowWord);
  }

  public void setUseSymSpellCheck(boolean use) {
    this.dictionary.setUseSymSpellCheck(use);
  }
}
