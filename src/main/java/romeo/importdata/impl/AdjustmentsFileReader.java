package romeo.importdata.impl;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import romeo.utils.XmlUtils;

/**
 * The adjustments xml file is used to make some adjustments to the default data
 * read from unit.csv when the default units are initialised. This includes such
 * things as adding acronymns etc. The "signature" attribute is used to identify
 * the unit via an md5 hash of its name (to avoid the need to duplicate
 * proprietary tables of info which the UC permissions for pleayer created
 * programs doesn't allow us to do). Other attributes (eg: "scanner") will match
 * properties of Unit.
 */
public class AdjustmentsFileReader {
  public static final String DEFAULT_PATH = "defaultAdjustments.xml";

  //.............

  private Resource _resource;

  /**
   * Create an AdjustmentsFileReader that will read defaultAdjustments.xml via
   * the classpath.
   */
  public AdjustmentsFileReader() {
    setResource(new ClassPathResource(DEFAULT_PATH));
  }

  /**
   * Create an AdjustmentsFileReader that will read the xml from the specified
   * resource.
   */
  public AdjustmentsFileReader(Resource resource) {
    setResource(resource);
  }

  /**
   * Returns a Map keyed by signature. Each entry value is another Map
   * containing the values keyed by attribute as read from the xml.
   * @return map
   */
  public Map<String, Map<String, String>> read() {
    try {
      Resource res = getResource();
      ;
      InputStream stream = res.getInputStream();
      try {
        if(stream == null) {
          throw new RuntimeException("null InputStream for resource " + res);
        }
        Map<String, Map<String, String>> map = new TreeMap<String, Map<String, String>>();
        Document document = XmlUtils.readDocument(stream);
        Element root = XmlUtils.getFirstChildElementWithName(document, "adjustments");
        Iterator<Element> i = XmlUtils.getChildElementsWithName(root, "adjustment").iterator();
        while(i.hasNext()) {
          Element element = (Element) i.next();
          String signature = element.getAttribute("signature");
          if(signature == null || signature.length() == 0) {
            throw new IllegalStateException("Missing signature in element");
          }
          Map<String, String> details = new TreeMap<String, String>();
          NamedNodeMap attributes = element.getAttributes();
          for(int a = 0; a < attributes.getLength(); a++) {
            Node attribute = attributes.item(a);
            String name = attribute.getNodeName();
            String value = attribute.getNodeValue();
            details.put(name, value);
          }
          map.put(signature, details);
        }
        return map;
      } finally {
        stream.close();
      }
    } catch(Exception e) {
      throw new RuntimeException("Error reading acronyms", e);
    }
  }

  /**
   * Get the Resource that is used to access the file to be read
   * @return resource
   */
  public Resource getResource() {
    return _resource;
  }

  /**
   * Set the Resource that is used to access the file containing the xml that
   * will be read
   * @param resource
   */
  public void setResource(Resource resource) {
    _resource = resource;
  }

}
