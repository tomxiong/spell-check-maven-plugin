package com.github.tomxiong.spellchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;

public class SpellListMojoTest extends AbstractMojoTestCase {

  @Test
  public void testExecute() {
    final SpellListMojo mojo = this.mojo();
    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    map.put("severity-family", "name,comments,help");
    map.put("simple-rule", "name,comments,help");
    map.put("message", "CDATA");
    mojo.map = (LinkedHashMap<String, String>) map;
    mojo.excludes = new String[]{"**/*.txt", "**/*.java"};
    //mojo.allowWord = new HashSet<>(Arrays.asList("foglight", "fglam", "pi"));
    //mojo.customDictionaryFile = new String[]{"src/test/resources/customWord.txt"};
    mojo.execute();
    //JavaSpellChecker checker = (JavaSpellChecker) mojo.checkersMap.get("java");
    //assertTrue(checker.getDictionary().countWords() > 80000);
    File file = new File("target/spelling_list_result.txt");
    assertTrue(file.exists());
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line = reader.readLine();
      assertTrue(line.startsWith("File"));
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  private SpellListMojo mojo() {
    File pom = getTestFile("pom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());
    final SpellListMojo mojo = new SpellListMojo();
    final MavenProject project = Mockito.mock(MavenProject.class);
    Mockito.doReturn(new File(".")).when(project).getBasedir();
    mojo.dirForScan = new File("src");
    mojo.outputDir = new File("target");
    //mojo.setSourceDirectory(new File("src"));
    //mojo.setTargetDirectory(new File("target"));
    return mojo;
  }

}
