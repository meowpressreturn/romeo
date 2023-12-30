
package romeo.importdata.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import romeo.utils.ClassPathFile;
import romeo.utils.XmlUtils;

/**
 * Reads xfactor expression text and description from an xml file.
 */
public class XFactorFileReader {
  public static final String DEFAULT_PATH = "defaultXFactors.xml";

  //.............

  private ClassPathFile _resource;

  /**
   * Constructor for an XFactorFileReader that will read the xfactors file from
   * the default path using a ClassPathResource.
   */
  public XFactorFileReader() {
    _resource = new ClassPathFile(DEFAULT_PATH);
  }

  /**
   * Open the resource and read the xfactors from the xml it contains, returning
   * this as a List of Map where the keys in the map are:
   * "name","description","trigger","attacks","offense","defense","remove","pd".
   * (These are all lowercase and the spelling of offense and defense as shown).
   * The corresponding values for these formulas are Strings. The linked units
   * for each xfactor are also in the map as a List of acronyms keyed by "link".
   * @return xfactors
   */
  public List<Map<String, Object>> read() {
    try {
      InputStream stream = _resource.getInputStream();
      try {
        if(stream == null) {
          throw new RuntimeException("null InputStream for resource " + _resource);
        }
        ArrayList<Map<String, Object>> xfactors = new ArrayList<Map<String, Object>>();
        Document document = XmlUtils.readDocument(stream);
        Element root = XmlUtils.getFirstChildElementWithName(document, "xfactors");
        Iterator<Element> xfIterator = XmlUtils.getChildElementsWithName(root, "xfactor").iterator();
        while(xfIterator.hasNext()) {
          Element xfElement = (Element) xfIterator.next();
          String name = XmlUtils.getValueOfNamedChild(xfElement, "name");
          if(name == null || name.length() == 0) {
            throw new RuntimeException("name not specified for an xfactor element");
          }
          String description = XmlUtils.getValueOfNamedChild(xfElement, "description");
          String trigger = XmlUtils.getValueOfNamedChild(xfElement, "trigger");
          String attacks = XmlUtils.getValueOfNamedChild(xfElement, "attacks");
          String offense = XmlUtils.getValueOfNamedChild(xfElement, "offense");
          String defense = XmlUtils.getValueOfNamedChild(xfElement, "defense");
          String remove = XmlUtils.getValueOfNamedChild(xfElement, "remove");
          String pd = XmlUtils.getValueOfNamedChild(xfElement, "pd");

          List<String> linkedUnits = Collections.emptyList(); //Will hold acronyms of units with this xfactor
          Element link = XmlUtils.getFirstChildElementWithName(xfElement, "link");
          if(link != null) {
            List<Element> unitElements = XmlUtils.getChildElementsWithName(link, "unit");
            Iterator<Element> units = unitElements.iterator();
            linkedUnits = new ArrayList<String>(unitElements.size());
            while(units.hasNext()) {
              Element unit = (Element) units.next();
              String acronymn = XmlUtils.getNodeText(unit);
              linkedUnits.add(acronymn);
            }
          }

          Map<String, Object> map = new TreeMap<String, Object>();
          map.put("name", name);
          map.put("description", description);
          map.put("trigger", trigger == null ? "" : trigger);
          map.put("attacks", attacks == null ? "" : attacks);
          map.put("offense", offense == null ? "" : offense);
          map.put("defense", defense == null ? "" : defense);
          map.put("remove", remove == null ? "" : remove);
          map.put("pd", pd == null ? "" : pd);
          map.put("link", linkedUnits);

          xfactors.add(map);
        }
        return xfactors;
      } finally {
        stream.close();
      }
    } catch(Exception e) {
      throw new RuntimeException("Error reading Xfactors", e);
    }
  }
}
