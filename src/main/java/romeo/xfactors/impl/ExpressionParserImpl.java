package romeo.xfactors.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.expressions.Adjust;
import romeo.xfactors.expressions.Arithmetic;
import romeo.xfactors.expressions.Comparison;
import romeo.xfactors.expressions.Context;
import romeo.xfactors.expressions.Fail;
import romeo.xfactors.expressions.Flag;
import romeo.xfactors.expressions.If;
import romeo.xfactors.expressions.Logic;
import romeo.xfactors.expressions.Present;
import romeo.xfactors.expressions.Quantity;
import romeo.xfactors.expressions.Rnd;
import romeo.xfactors.expressions.Value;

/**
 * Parses a single xf expression into the appropriate IExpression objects. A
 * reference to the unit service is required.
 */
public class ExpressionParserImpl implements IExpressionParser {
  
  ////////////////////////////////////////////////////////////////////////////
  
  public ExpressionParserImpl() {
    ;
  }

  /**
   * Given a string in XFEL (X Factor Expression Language) will parse it and
   * return an instance of {@link IExpression} whose evaluate() method will
   * return the appropriate value during battle simulations.
   * @param xfel
   * @return expression
   */
  @Override
  public IExpression getExpression(String xfel) {
    try {
      if(xfel == null || xfel.isEmpty()) {
        return null;
      }
      xfel = trimToken(xfel); //trim unwanted spaces and newlines around the outer expression
      if(xfel.isEmpty() || "NULL".equals(xfel.toUpperCase(Locale.US))) {
        return null;
      }
      int iolb = xfel.indexOf('('); //index of left bracket
      int iorb = xfel.lastIndexOf(')'); //index of right bracket
      if(iolb == -1 || iolb > iorb) {
        throw new IllegalArgumentException("Missing '(' in expression:" + xfel);
      }
      if(iorb == -1) {
        throw new IllegalArgumentException("Missing ')' in expression:" + xfel);
      }
      if(iorb < xfel.length() - 1) {
        throw new IllegalArgumentException(
            "Syntax error. Unexpected characters after closing ')':" + xfel.substring(iorb + 1, xfel.length()));
      }

      String exprType = xfel.substring(0, iolb).trim().toUpperCase();
      if(exprType.length() == 0) {
        throw new IllegalArgumentException("Missing expression type:" + xfel);
      }

      String params = xfel.substring(iolb + 1, iorb).trim(); //May be "" -in theory anyhow

      //Hardcoded expression types for now
      //todo - pull these from so kind of expression registry to allow for easy extension
      //todo - can use a switch for strings nowadays!
      if("ADJUST".equals(exprType)) {
        return new Adjust(params, this);
      } else if("ARITHMETIC".equals(exprType)) {
        return new Arithmetic(params, this);
      } else if("COMPARISON".equals(exprType)) {
        return new Comparison(params, this);
      } else if("CONTEXT".equals(exprType)) {
        return new Context(params, this);
      } else if("IF".equals(exprType)) {
        return new If(params, this);
      } else if("LOGIC".equals(exprType)) {
        return new Logic(params, this);
      } else if("QUANTITY".equals(exprType)) {
        return new Quantity(params, this);
      } else if("RND".equals(exprType)) {
        return new Rnd(params, this);
      } else if("VALUE".equals(exprType)) {
        return new Value(params, this);
      } else if("PRESENT".equals(exprType)) {
        return new Present(params, this);
      } else if("FLAG".equals(exprType)) {
        return new Flag(params, this);
      } else if("FAIL".equals(exprType)) {
        return new Fail(params, this);
      } else {
        throw new IllegalArgumentException("Unrecognised expression type:" + exprType + " in expression " + xfel);
      }
    } catch(IllegalArgumentException badExpr) {
      throw badExpr;
    } catch(Exception e) {
      throw new RuntimeException("Failed to parse expression:" + xfel, e);
    }
  }

  /**
   * Splits an XFEL params string into individual tokens for the top level of expression.
   * This process does not trim extraneous whitespace.
   * @param params
   * @return tokens
   */
  @Override
  public String[] tokenise(String params) {
    Objects.requireNonNull(params, "params may not be null");
    if(params.length() == 0) {
      return new String[0];
    }
    int bracketCount = 0;
    boolean inQuote = false;
    int index = 0;
    int length = params.length();
    int tokenStart = 0;
    List<String> tokens = new ArrayList<>(3);

    while(index < length) {
      char c = params.charAt(index);
      if(c == '"') {
        inQuote = !inQuote;
      } else if(c == '(' && !inQuote) {
        bracketCount++;
      } else if(c == ')' && !inQuote) {
        bracketCount--;
        if(bracketCount < 0) {
          throw new IllegalArgumentException("Unmatched bracket ')' at index " + index);
        }
      } else if(c == ',' && !inQuote && bracketCount < 1) {
        //end of token
        String token = params.substring(tokenStart, index);
        tokens.add(token);
        tokenStart = index + 1;
      }
      index++;
    } //end while < length
    
    if(bracketCount != 0) {
      throw new IllegalArgumentException("Unterminated '(' bracket in:" + params);
    }
    if(inQuote) {
      throw new IllegalArgumentException("Unterminated '\"' quote in:" + params);
    }
    
    //Add the final token to the list. (nb: if we ended with a ',' it will be an empty token).
    String token = params.substring(tokenStart, index);
    tokens.add(token);

    String[] array = new String[tokens.size()];
    for(int i = 0; i < array.length; i++) {
      array[i] = (String) tokens.get(i);
    }

    return array;
  }
  
  /**
   * Removes extraneous whitespace from an xfel token. This removes all whitespace that it
   * catches outside of a quoted section. (Whitespace is tabs, spaces, newlines)
   * This is intended for tokenising individual values of operands, and does not expect the
   * token it is passed to contain any subexpressions.
   * @param token
   * @return token
   */
  @Override
  public String trimToken(String token) {
    Objects.requireNonNull(token, "src may not be null");
    if(token.length() == 0) {
      return token;
    }
    int length = token.length();
    StringBuilder builder = new StringBuilder(length);
    boolean inQuote = false;
    int index = 0;
    while(index < length) {
      char c = token.charAt(index);
      if(c == '\"') {
        inQuote = !inQuote;
        builder.append(c);
      } else if(c == '\n' && !inQuote) {
        ; //Ignore
      } else if(c == ' ' && !inQuote) { 
        ; //Ignore
      } else if(c == '\t' && !inQuote) {
        ; //Ignore
      } else {
        builder.append(c);
      }
      index++;
    }
    return builder.toString();
  }

  /*
   * public static void main(String[] args) { String[] tests = new String[] {
   * "ARITHMETIC(VALUE(2),ADD,ARITHMETIC(ADJUST(ARITHMETIC(QUANTITY(THIS_PLAYER,NZF,null),DIVIDE,VALUE(100.0)),FLOOR),MIN,VALUE(5)))",
   * "ARITHMETIC(VALUE(2),ADD,VALUE(4))",
   * "COMPARISON(RND(1,100),GREATER_OR_EQUAL,ARITHMETIC(VALUE(50),MAX,QUANTITY(THAT_PLAYER,HC4,null)))",
   * "LOGIC(COMPARISON(RND(1,100),EQUAL,QUANTITY(ANY_PLAYER,X5,null)),AND,COMPARISON(CONTEXT(ROUND),GREATER_THAN,VALUE(8)))",
   * "IF(VALUE(true),LOGIC(VALUE(false),NOT,COMPARISON (  CONTEXT(IS_ATTACKER),EQUAL  ,VALUE(true))),   VALUE(\"foobar\n,  CONTEXT(ROUND),RND(1,100)\"))"
   * , }; ExpressionParserImpl testImpl = new ExpressionParserImpl(null); try {
   * String cleanMe =
   * "IF(VALUE (true),    LOGIC(VALUE(   false),NOT,COMPARISON(\nCONTEXT(IS_ATTACKER),EQUAL,VALUE(true)\n   ),VALUE(\"foobar\n,CONTEXT(ROUND),RND(1,100)\"))"
   * ; System.out.println("CLEANUP:" + cleanMe); System.out.println("CLEANED:" +
   * testImpl.cleanup(cleanMe)); System.out.println();
   * 
   * for(int i=0; i < tests.length; i++) { System.out.println("TESTING.... " +
   * tests[i]); IExpression expr = testImpl.getExpression(tests[i]);
   * System.out.println("RESULT..... " + expr.toString()); System.out.println();
   * } } catch(Exception e) { e.printStackTrace(); } }
   */
}
