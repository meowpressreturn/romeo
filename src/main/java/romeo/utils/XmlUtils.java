package romeo.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;

/**
 * Utility methods for working with XML
 */
public class XmlUtils {
  /**
   * Read an XML document from the input stream and parse it into a Document.
   * @param stream
   * @return document a DOM tree
   */
  public static Document readDocument(InputStream stream) {
    try {
      EntityResolver entityResolver = null;
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setExpandEntityReferences(false);
      DocumentBuilder builder = dbf.newDocumentBuilder();
      builder.setEntityResolver(entityResolver);
      return builder.parse(stream);
    } catch(Exception e) {
      throw new RuntimeException("Exception caught reading XML document", e);
    }
  }

  /**
   * Returns a List of Element that are children of the specified Node and have
   * the specified tag name.
   * @param node
   *          parent node
   * @param name
   *          node name to find
   * @return elements
   */
  public static List<Element> getChildElementsWithName(Node node, String nodeName) {
    if(node == null) {
      throw new NullPointerException("node is null");
    }
    ArrayList<Element> elements = new ArrayList<Element>();
    NodeList children = node.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if(child.getNodeType() == Node.ELEMENT_NODE) {
        if(nodeName.equals(child.getNodeName())) {
          elements.add((Element) child);
        }
      }
    }
    return elements;
  }

  /**
   * Returns the first child Element having the specified tag name
   * @param node
   * @param nodeName
   * @return element
   */
  public static Element getFirstChildElementWithName(Node node, String nodeName) {
    if(node == null) {
      throw new NullPointerException("node is null");
    }
    NodeList children = node.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if(child.getNodeType() == Node.ELEMENT_NODE) {
        if(nodeName.equals(child.getNodeName())) {
          return (Element) child;
        }
      }
    }
    return null;
  }

  /**
   * Returns the value of a child node having the specified node name
   * @param parent
   * @param elementName
   * @return value
   */
  public static String getValueOfNamedChild(Node parent, String childElementName) {
    if(parent == null) {
      throw new NullPointerException("parent is null");
    }
    Element child = XmlUtils.getFirstChildElementWithName(parent, childElementName);
    return XmlUtils.getNodeText(child);
  }

  /**
   * Returns the text of the specified node. This is done by concatenating TEXT
   * and CDATA children Does not recurse.
   * @param node
   * @return text
   */
  public static String getNodeText(Node node) {
    if(node == null) {
      return null;
    }
    StringBuffer buffer = new StringBuffer();
    NodeList contents = node.getChildNodes();
    for(int i = 0; i < contents.getLength(); i++) {
      Node contentNode = contents.item(i);
      switch (contentNode.getNodeType()){
        case Node.TEXT_NODE:
          buffer.append(contentNode.getNodeValue());
          break;

        case Node.CDATA_SECTION_NODE:
          buffer.append(trimNodeText(contentNode.getNodeValue()));
          break;
      }
    }
    return trimNodeText(buffer.toString());
  }

  /**
   * Trims spaces, tabs, and newlines from the start and end of the string.
   * @param text
   * @return trimmedText
   */
  public static String trimNodeText(String text) {
    if(text == null || text.length() == 0) {
      return text;
    }
    int length = text.length();
    int[] bounds = new int[2];
    bounds[0] = -1;
    bounds[1] = -1;
    boolean[] found = new boolean[2];
    found[0] = false;
    found[1] = false;
    boolean notDone = false;

    int i = 0;
    do {
      for(int b = 0; b < 2; b++) {
        if(!found[b]) {
          int index = b == 0 ? i : length - i;
          char c = text.charAt(b == 0 ? index : index - 1);
          switch (c){
            case ' ':
            case '\n':
            case '\t':
              break;
            default:
              if(!found[b]) {
                bounds[b] = index;
                found[b] = true;
              }
              break;
          }
        }
      }
      /*
       * System.out.println( "i=" + i + " bounds=" + bounds[0] + "," + bounds[1]
       * + " -->" + text.substring(bounds[0],bounds[1]) + "<--");
       */
      i++;
      notDone = i < length && !(found[0] && found[1]);
    } while(notDone);

    //System.out.println("bounds=" + bounds[0] + "," + bounds[1]);
    if(bounds[0] == -1 && bounds[1] == -1) {
      text = "";
    } else {
      text = text.substring(bounds[0], bounds[1]);
    }
    return text;
  }

  /*
   * public static void main(String[] args) { String[] testStrings = new
   * String[] { "   foo bar  ", "foo", "", "     ", " foo", "foo  ", "x    x",
   * "x", "   x   ", };
   * 
   * 
   * for(int i=0; i < testStrings.length; i++) { try { String test1 =
   * testStrings[i];
   * 
   * System.out.println("\nTest " + i + "\nInput >>>" + test1 + "<<<"); String
   * result = XmlUtils.trimNodeText(test1); System.out.println( "Result=>>>" +
   * result + "<<<" + ", Original length=" + test1.length() + ", result length="
   * + result.length()); } catch(Exception e) { System.out.println("Test " + i +
   * " FAILED: " + e.getMessage()); } } }
   */
}
