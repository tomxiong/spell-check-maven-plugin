package com.github.tomxiong.spellchecker.checker;

import com.github.tomxiong.spellchecker.dictionary.Dictionary;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlSpellChecker extends AbstractSpellChecker implements SpellChecker {

  private Set<String> skipCheck = new HashSet<>();

  public XmlSpellChecker(Dictionary dict, Log logger) {
    super(dict, logger);
  }

  @Override
  public List<String> tokenize(String line) {
    if (null == line || line.isEmpty()) {
      throw new IllegalArgumentException("line to tokenize shout not be null or empty");
    }
    return new LinkedList<>(findMatchedWords(line));
  }

  @Override
  public Collection<CheckResult> check(File file, boolean onlyList) {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    try {
      builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      builderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(file);
      Collection<CheckResult> fileResults = new LinkedHashSet<>();
      NodeList list = document.getChildNodes();
      for (int count = 0; count < list.getLength(); count++) {
        Node node = list.item(count);
        fileResults.addAll(checkNode(node, onlyList));
      }
      return fileResults;
    } catch (ParserConfigurationException | SAXException | IOException e) {
      getLogger().error(e);
    }
    return Collections.emptyList();
  }

  @Override
  protected boolean isValidLine(String trim) {
    return true;
  }

  private Collection<CheckResult> checkNode(Node node, boolean onlyList) {
    Collection<CheckResult> nodeResult = new LinkedHashSet<>();
    if (isValidNode(node.getNodeName())) {
      Set<String> attNamesNeedCheck = getCheckListMap().get(node.getNodeName());
      if (!attNamesNeedCheck.isEmpty()) {
        if (attNamesNeedCheck.contains("CDATA")) {
          checkTextContent(node, onlyList, nodeResult);
          checkNodeValue(node, onlyList, nodeResult);
        }
        checkAttributes(node, onlyList, nodeResult, attNamesNeedCheck);
      }
    }
    NodeList list = node.getChildNodes();
    for (int count = 0; count < list.getLength(); count++) {
      Node subNode = list.item(count);
      nodeResult.addAll(checkNode(subNode, onlyList));
    }
    return nodeResult;
  }

  private void checkAttributes(Node node, boolean onlyList, Collection<CheckResult> nodeResult,
      Set<String> attNamesNeedCheck) {
    NamedNodeMap nodeMap = node.getAttributes();
    if (nodeMap != null && nodeMap.getLength() > 0) {
      for (int count = 0; count < nodeMap.getLength(); count++) {
        Node attNode = nodeMap.item(count);
        if (isValidNodeAttribute(attNamesNeedCheck, attNode.getNodeName())) {
          checkAttributeNode(node, onlyList, nodeResult, attNode);
        }
      }
    }
  }

  private void checkAttributeNode(Node node, boolean onlyList, Collection<CheckResult> nodeResult,
      Node attNode) {
    String nodeValue = attNode.getNodeValue();
    if (nodeValue != null && !nodeValue.trim().isEmpty()) {
      if (onlyList) {
        nodeResult.add(new CheckResult(0, "[" + node.getNodeName() + "]'s attribute [" + attNode.getNodeName() + "] : " + nodeValue.trim(), null));
      }
      else {
        Map<String, Collection<String>> lineResult = checkLine(nodeValue.trim());
        if (!lineResult.isEmpty()) {
          nodeResult.add(new CheckResult(0, nodeValue.trim(), lineResult));
        }
      }
    }
  }

  private void checkTextContent(Node node, boolean onlyList, Collection<CheckResult> nodeResult) {
    String textContent = node.getTextContent();
    if (textContent != null && !textContent.trim().isEmpty() && isValidLine(textContent.trim())) {
      if (onlyList) {
        nodeResult.add(new CheckResult(0, "[" + node.getNodeName() + "]'s text : " + textContent.trim(), null));
      }
      else {
        Map<String, Collection<String>> lineResult = checkLine(textContent.trim());
        if (!lineResult.isEmpty()) {
          nodeResult.add(new CheckResult(0, textContent.trim(), lineResult));
        }
      }
    }
  }

  private void checkNodeValue(Node node, boolean onlyList, Collection<CheckResult> nodeResult) {
    String nodeValue = node.getNodeValue();
    if (nodeValue != null && !nodeValue.trim().isEmpty()) {
      if (onlyList) {
        nodeResult.add(new CheckResult(0, "[" + node.getNodeName() + "]'s value : " + nodeValue.trim(), null));
      }
      else {
        Map<String, Collection<String>> lineResult = checkLine(nodeValue.trim());
        if (!lineResult.isEmpty()) {
          nodeResult.add(new CheckResult(0, nodeValue.trim(), lineResult));
        }
      }
    }
  }

  private boolean isValidNodeAttribute(Set<String> attrs, String attrName) {
    return attrs.contains(attrName);
  }

  private boolean isValidNode(String nodeName) {
    if (skipCheck.contains(nodeName)) {
      return false;
    }
    return getCheckListMap().containsKey(nodeName);
  }

}
