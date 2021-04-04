package com.github.tomxiong.spellchecker.checker;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

public class JavaSpellChecker extends AbstractSpellChecker implements SpellChecker {

  public JavaSpellChecker(Dictionary dict, Log logger) {
    super(dict, logger);
  }

  public List<String> tokenize(String line) {
    if (null == line || line.isEmpty()) {
      throw new IllegalArgumentException("line to tokenize shout not be null or empty");
    }
    List<String> words = new LinkedList<>();
    int post = line.indexOf('\"');
    while (post > 0) {
      int nextPost = line.indexOf('\"', post + 1);
      if (nextPost > 0 && nextPost < line.length()) {
        String newline = line.substring(post + 1, nextPost);
        if (!newline.isEmpty()) {
          words.addAll(findMatchedWords(newline));
        }
        post = line.indexOf('\"', nextPost);
      } else {
        break;
      }
    }
    return words;
  }

  @Override
  protected boolean isValidLine(String line) {
    if (line.startsWith("/")) {
      return false;
    }
    if (line.startsWith("*")) {
      return false;
    }
    if (line.contains("=\"")) {
      return true;
    }
    if (line.contains("\"")) {
      if (isContainsEqualsWithText(line)) {
        return true;
      }

      return isContainsReturnWithText(line);
    }
    return false;
  }

  private boolean isContainsEqualsWithText(String line) {
    if (line.contains("=")) {
      int pos1 = line.indexOf("=");
      int pos2 = line.indexOf("\"");
      if (pos2 < pos1) {
        return false;
      }
      String subLine = line.substring(pos1 + 1, pos2);
      return subLine.trim().isEmpty();
    }
    return false;
  }

  private boolean isContainsReturnWithText(String line) {
    if (line.startsWith("return")) {
      int pos1 = line.indexOf("\"");
      return pos1 > 6 && line.substring(6, pos1).trim().isEmpty();
    }
    return false;
  }

}
