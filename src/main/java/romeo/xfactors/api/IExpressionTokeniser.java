package romeo.xfactors.api;

/**
 * API for breaking up parts of an XFEL string.
 */
public interface IExpressionTokeniser {

  /**
   * Parse the parameters portion of an XFEL string into its tokens
   * @param params
   * @return tokens
   */
  public String[] tokenise(String params);
  
  /**
   * Cleanup unwanted whitespace from an xfel token
   * @param token
   * @return
   */
  public String trimToken(String token);
  
}



















