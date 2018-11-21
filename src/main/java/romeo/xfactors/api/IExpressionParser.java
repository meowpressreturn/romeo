package romeo.xfactors.api;

/**
 * Expression Parser is used to construct an XFactor Expression from a String formatted as xfel.
 * nb:  Piublic methods for tokenising and trimming parts of such strings have been moved to {@link IExpressionTokeniser}.
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

}
