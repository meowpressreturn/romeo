package romeo.utils;

import java.io.InputStream;
import java.util.Objects;

/**
 * Represents a resource in the classpath. This class was created to take the place of Spring's
 * ClassPathResource (so we can drop the Spring dependency) but is considerably simpler.
 */
public class ClassPathFile {
  private final String _path;
  
  public ClassPathFile(String path) {
    _path = Objects.requireNonNull(path, "path may not be null");
  }
  
  public InputStream getInputStream() {
    return getClass().getClassLoader().getResourceAsStream(_path);
  }
  
  public boolean exists() {
    return getClass().getClassLoader().getResource(_path) != null;
  }
  
  public String toString() {
    return _path;
  }
}
