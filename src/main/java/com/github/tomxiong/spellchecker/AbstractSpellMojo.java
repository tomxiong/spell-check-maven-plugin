package com.github.tomxiong.spellchecker;

import static java.util.Objects.isNull;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import com.github.tomxiong.spellchecker.checker.CheckResult;
import com.github.tomxiong.spellchecker.checker.JavaSpellChecker;
import com.github.tomxiong.spellchecker.checker.PropertiesSpellChecker;
import com.github.tomxiong.spellchecker.checker.SpellChecker;
import com.github.tomxiong.spellchecker.checker.XmlSpellChecker;

public abstract class AbstractSpellMojo extends AbstractMojo {

  private static final String[] DEFAULT_EXCLUDES = new String[]{"**/pom.xml"};
  private static final String[] DEFAULT_INCLUDES = new String[]{"**/**"};
  @Parameter(property = "dirForScan", defaultValue = "${project.basedir}/src", required = false)
  public File dirForScan;
  @Parameter(property = "outputDir", defaultValue = "${project.basedir}/target", required = false)
  public File outputDir;
  @Parameter
  public String[] includes;
  @Parameter
  public String[] excludes;
  @Parameter(property = "skip", defaultValue = "false")
  public boolean skip = false;
  @Parameter
  public Map<String, String> map = new LinkedHashMap<>();
  @Parameter(property = "basedir", required = false)
  protected File baseDirectory;
  @Parameter(defaultValue = "${project}", readonly = true)
  protected MavenProject project;
  protected Map<String, SpellChecker> checkersMap = new HashMap<>();
  protected Map<String, String> languageMap = new HashMap<>();
  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;
  @Parameter(defaultValue = "${settings}", readonly = true)
  private Settings settings;

  protected String[] getIncludes() {
    if (includes != null && includes.length > 0) {
      return includes;
    }
    return DEFAULT_INCLUDES;
  }

  protected String[] getExcludes() {
    if (excludes != null && excludes.length > 0) {
      return excludes;
    }
    return DEFAULT_EXCLUDES;
  }

  protected String[] getSelectedFiles(File baseDirectory) {
    FileSetManager fileSetManager = new FileSetManager();
    FileSet checkFileSet = new FileSet();
    checkFileSet.setDirectory(baseDirectory.getAbsolutePath());
    checkFileSet.setIncludes(Arrays.asList(getIncludes()));
    checkFileSet.setExcludes(Arrays.asList(getExcludes()));
    return fileSetManager.getIncludedFiles(checkFileSet);
  }

  public String getExtensionByGuava(String filename) {
    return Files.getFileExtension(filename);
  }

  protected void initialCheckers(Dictionary dictionary) {
    checkersMap.put("properties", new PropertiesSpellChecker(dictionary, getLog()));
    checkersMap.put("java", new JavaSpellChecker(dictionary, getLog()));
    languageMap.put("groovy", "java");

    if (!isNull(map) && !map.isEmpty()) {
      XmlSpellChecker checker = new XmlSpellChecker(dictionary, getLog());
      checkersMap.put("xml", checker);
      checker.addCustomCheckList(map);
    }
  }

  protected void generateReport(String reportFileName, Map<String, Collection<CheckResult>> checkResultMap) {
    File reportFile = new File(outputDir, reportFileName);
    try {
      if (reportFile.exists()) {
        java.nio.file.Files.delete(reportFile.toPath());
      }
      if (!reportFile.createNewFile()) {
        throw new FileAlreadyExistsException(reportFile.getPath());
      }
    } catch (IOException e) {
      getLog().error("Failed to create report file: " + reportFile.getPath(), e);
    }
    try (FileWriter writer = new FileWriter((reportFile))) {
      for (Map.Entry<String, Collection<CheckResult>> checkResults : checkResultMap.entrySet()) {
        writer.write("File ");
        writer.write(dirForScan.getCanonicalPath());
        writer.write(File.separator);
        writer.write(checkResults.getKey());
        writer.write(System.lineSeparator());
        for (CheckResult checkResult : checkResults.getValue()) {
          writer.write(checkResult.toString());
        }
        writer.write(System.lineSeparator());
      }
      getLog().info("Spelling check result : " + reportFile.getCanonicalPath());
    } catch (IOException e) {
      getLog().error(e.getMessage(), e);
    }
  }

  protected Collection<CheckResult> checkFile(File file, boolean onlyList) {
    if (file.isFile()) {
      String fileExtensionName = getExtensionByGuava(file.getName());
      SpellChecker checker = checkersMap.get(fileExtensionName);
      if (checker == null) {
        checker = checkersMap.get(languageMap.get(fileExtensionName));
        if (checker != null) {
          checkersMap.put(fileExtensionName, checker);
        }
      }
      if (checker != null) {
        return checker.check(file, onlyList);
      }
    }
    return Collections.emptyList();
  }

  protected void checkFilesAndGenerateReport(File baseDirectory, boolean onlyList) {
    String[] selectedFiles = getSelectedFiles(baseDirectory);

    Map<String, Collection<CheckResult>> checkResultMap = new LinkedHashMap<>();
    for (String selectedFileName : selectedFiles) {
      File selectedFile = new File(baseDirectory, selectedFileName);
      Collection<CheckResult> fileResult = checkFile(selectedFile, onlyList);
      if (!fileResult.isEmpty()) {
        checkResultMap.put(selectedFileName, fileResult);
      }
    }
    if (!checkResultMap.isEmpty()) {
      if (onlyList) {
        generateReport("spelling_list_result.txt", checkResultMap);
      }
      else {
        generateReport("spelling_check_result.txt", checkResultMap);
      }
    }
  }

  protected boolean invalidRequiredArguments() {
    if (skip) {
      getLog().warn("skipped");
      return true;
    }
    if (isNull(dirForScan) || !dirForScan.exists()) {
      getLog().warn("Skipped" + System.lineSeparator()
          + "Make sure you are specifying the correct source directory.");
      return true;
    }
    if (baseDirectory == null && project != null) {
      baseDirectory = project.getBasedir();
    }
    return false;
  }
}
