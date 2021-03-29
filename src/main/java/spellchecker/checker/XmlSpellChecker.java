package spellchecker.checker;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import spellchecker.dictionary.Dictionary;

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
    List<String> words = new LinkedList<>();
    words.addAll(findMatchedWords(line));
    return words;
  }

  @Override
  public Collection<CheckResult> check(File file, boolean onlyList) {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(file);
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        getLogger().debug("Parsed file : " + file.getName());
      }
      Collection<CheckResult> fileResults = new LinkedHashSet<>();
      NodeList list = document.getChildNodes();
      for (int count = 0; count < list.getLength(); count++) {
        Node node = list.item(count);
        fileResults.addAll(checkNode(node, onlyList));
      }
      return fileResults;
    } catch (ParserConfigurationException | SAXException | IOException e) {
      if (getLogger() != null) {
        getLogger().error(e);
      } else {
        e.printStackTrace();
      }
    }
    return Collections.EMPTY_SET;
  }

  @Override
  protected boolean isValidLine(String trim) {
    return isAlpha(trim);
  }

  private Collection<CheckResult> checkNode(Node node, boolean onlyList) {
    Collection<CheckResult> nodeResult = new LinkedHashSet<>();
    if (getLogger() != null && getLogger().isDebugEnabled()) {
      //getLogger().debug("Checking node : " + node.getNodeName());
    }
    if (isValidNode(node.getNodeName())) {
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        getLogger().debug("Checking node " + node.getNodeName());
      }
      Set<String> attNamesNeedCheck = getCheckListMap().get(node.getNodeName());
      if (!attNamesNeedCheck.isEmpty()) {
        if (getLogger() != null && getLogger().isDebugEnabled()) {
          getLogger().debug("Checking attributes : " + node.getNodeName());
        }
        if (attNamesNeedCheck.contains("CDATA")) {
          String textContent = node.getTextContent();
          if (textContent != null && !textContent.trim().isEmpty() && isValidLine(textContent.trim())) {
            if (getLogger() != null && getLogger().isDebugEnabled()) {
              getLogger().debug("Checking text " + textContent);
            }
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

          String nodeValue = node.getNodeValue();
          if (nodeValue != null && !nodeValue.trim().isEmpty()) {
            if (getLogger() != null && getLogger().isDebugEnabled()) {
              getLogger().debug("Checking value " + nodeValue);
            }
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
        NamedNodeMap nodeMap = node.getAttributes();
        if (nodeMap != null && nodeMap.getLength() > 0) {
          for (int count = 0; count < nodeMap.getLength(); count++) {
            Node attNode = nodeMap.item(count);
            if (isValidNodeAttribute(attNamesNeedCheck, attNode.getNodeName())) {
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
          }
        }
      }
    }
    NodeList list = node.getChildNodes();
    for (int count = 0; count < list.getLength(); count++) {
      Node subNode = list.item(count);
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        //getLogger().debug("Check " +node.getNodeName() + "'s sub nodes.");
      }
      nodeResult.addAll(checkNode(subNode, onlyList));
    }
    return nodeResult;
  }

  private boolean isValidNodeAttribute(Set<String> attrs, String attrName) {
    return attrs.contains(attrName);
  }

  private boolean isValidNode(String nodeName) {
    if (skipCheck.contains(nodeName)) {
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        getLogger().debug("Skip node " + nodeName);
      }
      return false;
    }
    if (getCheckListMap().containsKey(nodeName)) {
      if (getLogger() != null && getLogger().isDebugEnabled()) {
        getLogger().debug("Need to check node " + nodeName);
      }
      return true;
    }
    if (getLogger() != null && getLogger().isDebugEnabled()) {
      //getLogger().debug("The node " + nodeName + " is not valid.");
    }
    return false;
  }

}
