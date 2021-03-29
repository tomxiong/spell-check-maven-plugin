package spellchecker.checker;

import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
import spellchecker.dictionary.Dictionary;

public class PropertiesSpellChecker extends AbstractSpellChecker implements SpellChecker {

  public PropertiesSpellChecker(Dictionary dict, Log logger) {
    super(dict, logger);
  }

  public List<String> tokenize(String line) {
    if (null == line || line.isEmpty()) {
      throw new IllegalArgumentException("line to tokenize shout not be null or empty");
    }
    List<String> words = new LinkedList<>();
    int post = line.indexOf('=');
    if (post > 0) {
      String newline = line.substring(post + 1);
      if (newline != null && !newline.isEmpty()) {
        words.addAll(findMatchedWords(newline));
      }
    }
    return words;
  }

  @Override
  protected boolean isValidLine(String line) {
    if (line.startsWith("/") || line.startsWith("#")) {
      return false;
    }
    return line.contains("=");
  }
}
