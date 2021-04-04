package com.github.tomxiong.spellchecker;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "list", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class SpellListMojo extends AbstractSpellMojo {

  @Override
  public void execute() {
    if (invalidRequiredArguments()) {
      return;
    }

    initialCheckers(null);

    checkFilesAndGenerateReport(dirForScan, true);
  }

}
