package romeo.xfactors.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;
import romeo.xfactors.expressions.Adjust;
import romeo.xfactors.expressions.Adjust.AdjustOperand;
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
public class ExpressionParserImpl implements IExpressionParser, IExpressionTokeniser {
  
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

      switch(exprType) {
        case "ADJUST": return parseAdjust(params);
       
        default: {
          if("ARITHMETIC".equals(exprType)) {
            return new Arithmetic(params, this, this);
          } else if("COMPARISON".equals(exprType)) {
            return new Comparison(params, this, this);
          } else if("CONTEXT".equals(exprType)) {
            return new Context(params, this, this);
          } else if("IF".equals(exprType)) {
            return new If(params, this, this);
          } else if("LOGIC".equals(exprType)) {
            return new Logic(params, this, this);
          } else if("QUANTITY".equals(exprType)) {
            return new Quantity(params, this, this);
          } else if("RND".equals(exprType)) {
            return new Rnd(params, this, this);
          } else if("VALUE".equals(exprType)) {
            return new Value(params, this, this);
          } else if("PRESENT".equals(exprType)) {
            return new Present(params, this, this);
          } else if("FLAG".equals(exprType)) {
            return new Flag(params, this, this);
          } else if("FAIL".equals(exprType)) {
            return new Fail(params, this, this);
          } else {
            throw new IllegalArgumentException("Unrecognised expression type:" + exprType + " in expression " + xfel);
          }          
        }
      } //end switch exprType
      
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
  
  public Adjust parseAdjust(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 2) {
        throw new IllegalArgumentException("Expecting 2 parameters but found " + tokens.length);
      }
      IExpression value = getExpression(tokens[0]);
      AdjustOperand operand = AdjustOperand.fromString( trimToken(tokens[1]) );
      return new Adjust(value, operand);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to create ADJUST with params:" + params, e);
    }
  }
}
