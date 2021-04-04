package com.github.tomxiong.spellchecker.checker;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class CheckResult {

  int lineNum;
  String line;
  Map<String, Collection<String>> suggestions;

  public CheckResult(int lineNum, String line, Map<String, Collection<String>> suggestions) {
    this.lineNum = lineNum;
    this.line = line;
    this.suggestions = suggestions;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (suggestions != null && suggestions.size() > 0) {
      for (Map.Entry<String, Collection<String>> wordResult : suggestions.entrySet()) {
        if (lineNum > 0) {
          result.append("Line ").append(lineNum).append(" ");
        }
        result.append(wordResult.getKey()).append(", replacement suggestions:")
              .append(wordResult.getValue().toString());
        result.append(System.lineSeparator());
      }
    }
    else {
      if (!StringUtils.isEmpty(line)) {
        if (lineNum > 0) {
          result.append("Line ").append(lineNum).append(" ");
        }
        result.append(line);
        result.append(System.lineSeparator());
      }
    }
    return result.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CheckResult that = (CheckResult) o;
    if (lineNum == that.lineNum) {
      return this.line.equals(that.line);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lineNum, line);
  }
}
