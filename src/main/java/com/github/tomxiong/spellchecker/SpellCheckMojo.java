package com.github.tomxiong.spellchecker;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.github.tomxiong.spellchecker.checker.SpellChecker;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class SpellCheckMojo extends AbstractSpellMojo {

  @Parameter
  public Set<String> allowWord = new HashSet<>();
  @Parameter
  public String[] customDictionaryFile;
  @Parameter(defaultValue = "true")
  public boolean useSymSpellCheck;
  @Parameter(defaultValue = "false")
  public boolean useDamerauLevenshteinDistance;
  @Parameter(defaultValue = "true")
  public boolean useCheckCache;

  public void execute() {
    if (invalidRequiredArguments()) {
      return;
    }

    Dictionary dictionary = Dictionary.getInstance();
    if (!useCheckCache) {
      dictionary.clearCache();
    }
    dictionary.setLogger(getLog());
    dictionary.setUseSymSpellCheck(useSymSpellCheck);
    dictionary.setUseDamerauLevenshteinDistance(useDamerauLevenshteinDistance);

    if (customDictionaryFile != null && customDictionaryFile.length > 0) {
      loadCustomDictionaries(dictionary, customDictionaryFile, baseDirectory);
    }

    initialCheckers(dictionary);

    if (!allowWord.isEmpty()) {
      for (SpellChecker checker : checkersMap.values()) {
        checker.addAllowWord(allowWord);
      }
    }

    checkFilesAndGenerateReport(dirForScan, false);

  }

  private void loadCustomDictionaries(Dictionary dictionary, String[] customDictFile, File baseDir) {
    for (String fileName : customDictFile) {
      if (fileName != null && !fileName.trim().isEmpty()) {
        File dictionaryFile = new File(baseDir, fileName);
        if (!dictionaryFile.exists()) {
          dictionaryFile = new File(fileName);
        }
        if (dictionaryFile.exists()) {
          dictionary.loadCustomDictionary(dictionaryFile);
        }
      }
    }
  }

}
