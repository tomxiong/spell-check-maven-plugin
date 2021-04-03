package com.github.tomxiong.spellchecker;

import static java.util.Objects.isNull;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import java.io.File;
import java.util.HashSet;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import com.github.tomxiong.spellchecker.checker.SpellChecker;

@Mojo(name = "check", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class SpellCheckMojo extends AbstractSpellMojo {

  @Parameter
  public HashSet<String> allowWord = new HashSet<>();
  @Parameter
  public String[] customDictionaryFile;
  @Parameter(defaultValue = "true")
  public boolean useSymSpellCheck;
  @Parameter(defaultValue = "false")
  public boolean useDamerauLevenshteinDistance;
  @Parameter(defaultValue = "true")
  public boolean useCheckCache;

  public SpellCheckMojo() {
  }

  public void execute() {
    if (skip) {
      getLog().warn("skipped");
      return;
    }
    if (isNull(dirForScan) || !dirForScan.exists()) {
      getLog().warn("Skipped" + System.lineSeparator()
          + "Make sure you are specifying the correct source directory.");
      return;
    }
    if (baseDirectory == null) {
      if (project != null) {
        baseDirectory = project.getBasedir();
      }
    }

    Dictionary dictionary = Dictionary.getInstance();
    if (!useCheckCache) {
      if (getLog() != null && getLog().isDebugEnabled()) {
        getLog().debug("It will clear cache.");
      }
      dictionary.clearCache();
    }
    dictionary.setLogger(getLog());
    dictionary.setUseSymSpellCheck(useSymSpellCheck);
    dictionary.setUseDamerauLevenshteinDistance(useDamerauLevenshteinDistance);

    if (getLog().isDebugEnabled()) {
      getLog().debug("The dictionary size is " + dictionary.countWords());
      getLog().debug("Load the custom word dictionary from: " + customDictionaryFile[0]);
    }
    if (customDictionaryFile != null && customDictionaryFile.length > 0) {
      for (String fileName : customDictionaryFile) {
        //getLog().debug("the loading " + count  + " file : " + fileName);
        if (fileName != null && !fileName.trim().isEmpty()) {
          if (getLog().isDebugEnabled()) {
            getLog().debug("Load the custom word dictionary from: " + fileName);
          }
          File dictionaryFile = new File(baseDirectory, fileName);
          if (!dictionaryFile.exists()) {
            if (getLog().isDebugEnabled()) {
              getLog().debug("The custom word dictionary files is not abstract path: " + fileName);
            }
            dictionaryFile = new File(fileName);
          }
          if (dictionaryFile.exists()) {
            if (getLog().isDebugEnabled()) {
              getLog().debug("The custom word dictionary files is full path: " + fileName);
            }
            dictionary.loadCustomDictionary(dictionaryFile);
          }
        }
      }
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug("The dictionary size is " + dictionary.countWords());
    }

    initialCheckers(dictionary);

    if (!allowWord.isEmpty()) {
      if (getLog().isDebugEnabled()) {
        getLog().debug("Add allowWord :" + allowWord);
      }
      for (SpellChecker checker : checkersMap.values()) {
        checker.addAllowWord(allowWord);
      }
    }

    checkFilesAndGenerateReport(dirForScan, false);

  }

}
