package com.github.tomxiong.spellchecker.dictionary;

import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.QwertyDistance;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.maven.plugin.logging.Log;

public class Dictionary {

  private static final int MAX_WORD_DIFFERENCE_IN_LENGTH = 3;
  private static Dictionary dictionary;
  private static Pattern NUMBER_PATTERN = Pattern.compile("[0x|0X]?+\\d{1,}[l|L]?+");

  private static Pattern DECIMAL_PATTERN = Pattern.compile("[0x|0X]?+\\d*.\\d*[d|D|f|F]?+");
  private static int MAX_EDIT_DISTANCE = 2;
  private static int MAX_RESULT = 10;
  protected Log logger;
  private Map<Integer, Set<String>> WORDS = new LinkedHashMap<>();
  private Map<String, List<String>> CacheRESULTS = new LinkedHashMap<>();
  private SpellCheckSettings spellCheckSettings;

  private DataHolder dataHolder;

  private SymSpellCheck symSpellCheck;

  private boolean isUseSymSpellCheck;

  private SpellSuggester suggester;

  private Dictionary(String file) {
    loadDictionaryByResource(file);
  }

  public static synchronized Dictionary getInstance() {
    if (dictionary == null) {
      Dictionary tempDict = new Dictionary("en_us.dic");
      tempDict.loadDictionaryByResource("en-80k.txt");
      tempDict.loadDictionaryByResource("words_alpha.txt");
      tempDict.loadDictionaryByResource("frequency_dictionary_en_82_765.txt");
      tempDict.initSymSpellChecker();
      tempDict.initSuggester();
      dictionary = tempDict;
    }
    return dictionary;
  }

  private void initSuggester() {
    suggester = SpellSuggester.getInstance(this.WORDS);
  }

  protected static synchronized Dictionary getInstance(String dictionaryFile) {
    if (dictionary == null) {
      dictionary = new Dictionary(dictionaryFile);
    }
    return dictionary;
  }

  private static void loadUniGramFile(DataHolder dataHolder, String file)
      throws IOException, SpellCheckException {
    try (InputStream inputStream = Dictionary.class.getClassLoader().getResourceAsStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split("\\s+");
        dataHolder.addItem(new DictionaryItem(arr[0], Double.parseDouble(arr[1]), -1.0));
      }
    }
  }

  private static void loadBiGramFile(DataHolder dataHolder, String file)
      throws IOException, SpellCheckException {
    try (InputStream inputStream = Dictionary.class.getClassLoader().getResourceAsStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] arr = line.split("\\s+");
        dataHolder
            .addItem(new DictionaryItem(arr[0] + " " + arr[1], Double.parseDouble(arr[2]), -1.0));
      }
    }
  }

  private void loadDictionaryByResource(String fileName) {
    try (InputStream inputStream = Dictionary.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
      if (logger != null && logger.isDebugEnabled()) {
        logger.debug("Before load dictionary " + fileName + " " + countWords());
      }
      WORDS = parseWordsFromDict(bufferedReader, WORDS);
      if (logger != null && logger.isDebugEnabled()) {
        logger.debug("After load dictionary " + fileName + " " + countWords());
      }
    } catch (IOException e) {
      WORDS.clear();
      e.printStackTrace();
    }
  }

  protected Map<Integer, Set<String>> parseWordsFromDict(BufferedReader reader,
      Map<Integer, Set<String>> words) throws IOException {
    String line = reader.readLine();
    while (null != line) {
      line = line.trim();
      if (line.contains(" ")) {
        line = line.substring(0, line.indexOf(" "));
      }
      Set<String> wordSet = words.get(line.length());
      if (null == wordSet) {
        wordSet = new LinkedHashSet<>();
        wordSet.add(line);
        words.put(line.length(), wordSet);
      } else {
        wordSet.add(line);
      }
      line = reader.readLine();
    }
    return words;
  }

  private void loadDictionary(File dictFile) {
    try (InputStream inputStream = new FileInputStream(dictFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        getLogger()
            .debug("Before load dictionary " + dictFile.getCanonicalPath() + " " + countWords());
      }
      WORDS = parseWordsFromDict(bufferedReader, WORDS);
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        getLogger()
            .debug("After load dictionary " + dictFile.getCanonicalPath() + " " + countWords());
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  protected void initSymSpellChecker() {
    spellCheckSettings = SpellCheckSettings.builder()
        .countThreshold(1)
        .deletionWeight(1)
        .insertionWeight(1)
        .replaceWeight(1)
        .maxEditDistance(MAX_EDIT_DISTANCE)
        .transpositionWeight(1)
        .topK(5)
        .prefixLength(23)
        .verbosity(Verbosity.ALL).build();
    WeightedDamerauLevenshteinDistance weightedDamerauLevenshteinDistance =
        new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
            spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
            spellCheckSettings.getTranspositionWeight(), new QwertyDistance());
    dataHolder = new InMemoryDataHolder(spellCheckSettings, new Murmur3HashFunction());

    symSpellCheck = new SymSpellCheck(dataHolder, weightedDamerauLevenshteinDistance,
        spellCheckSettings);

    try {
      loadUniGramFile(dataHolder, "frequency_dictionary_en_82_765.txt");
      loadBiGramFile(dataHolder, "frequency_bigramdictionary_en_243_342.txt");
    } catch (IOException | SpellCheckException e) {
      getLogger().error(e.getMessage(), e);
    }
  }

  public Log getLogger() {
    return logger;
  }

  public void setLogger(Log logger) {
    this.logger = logger;
  }

  public void loadCustomDictionary(File dictFile) {
    loadDictionary(dictFile);
  }

  public long countWords() {
    long count = 0;
    for (Set<String> collection : this.WORDS.values()) {
      count = count + collection.size();
    }
    return count;
  }

  public boolean isWord(String word) {
    Set<String> dictByLength = this.WORDS.get(word.length());
    return (dictByLength != null && dictByLength.contains(word));
/*
    Matcher matcher = NUMBER_PATTERN.matcher(word);
    if (matcher.matches()) {
      return true;
    }

    matcher = DECIMAL_PATTERN.matcher(word);
    return matcher.matches();*/
  }

  public List<String> suggest(String word) {
    if (CacheRESULTS.containsKey(word.toLowerCase())) {
      return CacheRESULTS.get(word.toLowerCase());
    }

    Map<Integer, List<String>> results = suggest(word.toLowerCase(),
        Math.min(MAX_WORD_DIFFERENCE_IN_LENGTH, word.length() / 2));

    List<String> result = new ArrayList<>();
    List<String> zeroDistance = results.get(0);
    if (zeroDistance != null && zeroDistance.isEmpty()) {
      result.addAll(zeroDistance);
    }
    for (int i = 1; i <= MAX_EDIT_DISTANCE; i++) {
      List<String> row = results.get(i);
      if (row != null && !row.isEmpty()) {
        for (String rankedWord : row) {
          if (result.size() < MAX_RESULT) {
            result.add(rankedWord);
          }
        }
      }
    }
    CacheRESULTS.put(word.toLowerCase(), result);
    return result;
  }

  /**
   * This method returns list of possible correct words.
   *
   * @param word Word similar to which we will find.
   * @param tol  maximum number of variance allowed in length.
   * @return A map of lists of words, whose key is edit-length.
   */
  private Map<Integer, List<String>> suggest(String word, int tol) {
    if (null == word || word.isEmpty()) {
      throw new IllegalArgumentException("The parameter 'word' is null or empty.");
    }

    if (isUseSymSpellCheck) {
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        getLogger().debug("Using symspell checker.");
      }
      return getSuggestionBySymSpellChecker(word);
    } else {
      return this.suggester.suggest(word, tol, MAX_EDIT_DISTANCE);
    }
  }

  private Map<Integer, List<String>> getSuggestionBySymSpellChecker(String word) {
    Map<Integer, List<String>> result = new LinkedHashMap<>();
    try {
      List<SuggestionItem> suggestions = symSpellCheck.lookup(word, Verbosity.ALL);
      if (word.length() > MAX_WORD_DIFFERENCE_IN_LENGTH * 2) {
        suggestions.addAll(symSpellCheck.lookupCompound(word, 1, false));
      }
      for (SuggestionItem suggestionItem : suggestions) {
        Integer distance = (int) Math.round(suggestionItem.getDistance());
        List<String> suggests = result.get(distance);
        if (suggests == null || suggests.isEmpty()) {
          suggests = new LinkedList<>();
        }
        if (!suggests.contains(suggestionItem.getTerm())) {
          suggests.add(suggestionItem.getTerm());
        }
        result.put(distance, suggests);
      }
    } catch (SpellCheckException e) {
      getLogger().error(e.getMessage(), e);
    }
    return result;
  }

  public boolean isUseSymSpellCheck() {
    return isUseSymSpellCheck;
  }

  public void setUseSymSpellCheck(boolean useSymSpellCheck) {
    isUseSymSpellCheck = useSymSpellCheck;
  }

  public void clearCache() {
    this.CacheRESULTS.clear();
  }

  public void setUseDamerauLevenshteinDistance(boolean useDamerauLevenshteinDistance) {
    if (useDamerauLevenshteinDistance) {
      this.suggester.setStringDistance(
          new WeightedDamerauLevenshteinDistance(spellCheckSettings.getDeletionWeight(),
              spellCheckSettings.getInsertionWeight(), spellCheckSettings.getReplaceWeight(),
              spellCheckSettings.getTranspositionWeight(), new QwertyDistance()));
    }
  }
}
