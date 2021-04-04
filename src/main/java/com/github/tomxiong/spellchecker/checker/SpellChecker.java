package com.github.tomxiong.spellchecker.checker;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public interface SpellChecker {

  Collection<CheckResult> check(File file, boolean onlyList);

  void addAllowWord(Set<String> allowWord);
}
