package spellchecker;

import static java.util.Objects.isNull;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "list", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class SpellListMojo extends AbstractSpellMojo {

  @Override
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

    initialCheckers(null);

    checkFilesAndGenerateReport(dirForScan, true);
  }

}
