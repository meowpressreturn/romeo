/*
 * IExpressionFactory.java
 * Created on Mar 13, 2006
 */
package romeo.xfactors.api;

/**
 * Expression Parser is used to construct an XFactor Expression from a String formatted as xfel, and provides
 * services for tokenising and trimming parts of such strings.
 */
public interface IExpressionParser {
  /**
   * Given an expression string will parse it and return an object that
   * implements IExpression (usually a composite object internally composed from
   * various classes that also implement IExpression). The IExpression can be
   * used to obtain the actual result of an xfactor calculation given a
   * RoundContext.
   * nb: for empty, null, or the literal String "NULL" the parser will return a
   * null value rather than throw an exception.
   * @param exprString
   * @return expression or null
   * @throws IllegalArgumentException
   *           if the expression string is invalid
   */
  public IExpression getExpression(String exprStr);

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
