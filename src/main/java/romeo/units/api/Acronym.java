package romeo.units.api;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable 'value object' to represent a unit acronym. We give units an
 * 'acronym' which is basically just a short name that it can be conveniently
 * referred to by in the UI and XFEL expressions. (It doesn't actually have to
 * be an acronym of the real name, but most are as this is easier to remember
 * and type.) Acronyms are case-insensitive. They retain the original
 * (trimmed) cased text for display purposes, but internally use the uppercased
 * text for all comparison.
 */
public class Acronym {
  
  /**
   * Factory method to create an acronym from a given string. 
   * Acronym test _will_ automatically be trimmed.
   * @param text may not be null or empty
   * @return acronym
   */
  public static Acronym fromString(String text) {
    return new Acronym(text);
  }

  private final String _text;
  private final String _displayText;
  
  /**
   * Constructor is hidden. Use the static factory method.
   * @param text
   */
  private Acronym(final String text) {
    _displayText = Objects.requireNonNull(text, "acronym text may not be null").trim();
    if(_displayText.isEmpty()) {
      throw new IllegalArgumentException("acronym text may not be empty");
    }
  //TODO - use a regex to make sure it is sensible
    //nb: this has backward compatibility implications
    _text = _displayText.toUpperCase(Locale.US);
  }
  
  /**
   * An acronym is equal to another if it has the same uppercased text. Differences in
   * the display text are ignored here.
   */
  @Override
  public boolean equals(Object o) {
    if(this==o) {
      return true;
    } else if(o==null || !(o instanceof Acronym)) {
      return false;
    } else {
      return _text.equals(((Acronym)o)._text);    
    }
  }
  
  /**
   * Uses the hashCode of the normalised (uppercased) acronym text
   * @return hashCode
   */
  @Override
  public int hashCode() {
    return _text.hashCode();
  }
  
  /**
   * Returns the display text, which can be cased
   * @return displayText
   */
  @Override
  public String toString() {
    return _displayText;
  }
  
  /**
   * Returns the normalised acronym text (ie: uppercased)
   * @return text
   */
  public String getText() {
    return _text;
  }
  
}



















