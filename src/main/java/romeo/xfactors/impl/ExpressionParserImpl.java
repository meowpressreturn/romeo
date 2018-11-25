package romeo.xfactors.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import romeo.fleet.model.SourceId;
import romeo.utils.Convert;
import romeo.xfactors.api.IExpression;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.expressions.Adjust;
import romeo.xfactors.expressions.Adjust.AdjustOperand;
import romeo.xfactors.expressions.Arithmetic;
import romeo.xfactors.expressions.Arithmetic.ArithmeticOperand;
import romeo.xfactors.expressions.Comparison;
import romeo.xfactors.expressions.Comparison.ComparisonOperand;
import romeo.xfactors.expressions.Context;
import romeo.xfactors.expressions.Context.ContextOperand;
import romeo.xfactors.expressions.Fail;
import romeo.xfactors.expressions.Flag;
import romeo.xfactors.expressions.Flag.FlagOperand;
import romeo.xfactors.expressions.If;
import romeo.xfactors.expressions.Logic;
import romeo.xfactors.expressions.Logic.LogicOperand;
import romeo.xfactors.expressions.Present;
import romeo.xfactors.expressions.Quantity;
import romeo.xfactors.expressions.Quantity.QuantityOperand;
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

      switch(exprType) {
        case "ADJUST" : return parseAdjust(params);
        case "ARITHMETIC" : return parseArithmetic(params);
        case "COMPARISON" : return parseComparison(params);
        case "CONTEXT" : return parseContext(params);
        case "FAIL" : return parseFail(params);
        case "FLAG" : return parseFlag(params);
        case "IF" : return parseIf(params);
        case "LOGIC" : return parseLogic(params);
        case "PRESENT" : return parsePresent(params);
        case "QUANTITY" : return parseQuantity(params);
        case "RND" : return parseRnd(params);
        case "VALUE" : return parseValue(params);
       
        default:
          throw new IllegalArgumentException("Unrecognised expression type:" + exprType + " in expression " + xfel);
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
  
  public Arithmetic parseArithmetic(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      IExpression left = getExpression(tokens[0]);
      ArithmeticOperand operand = ArithmeticOperand.fromString( trimToken(tokens[1]));
      IExpression right = getExpression(tokens[2]);
      return new Arithmetic(left, operand, right);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise ARITHMETIC with params:" + params, e);
    }
  }
  
  public Comparison parseComparison(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      IExpression left = getExpression(tokens[0]);
      ComparisonOperand operand = ComparisonOperand.fromString(trimToken(tokens[1]));
      IExpression right = getExpression(tokens[2]);
      return new Comparison(left, operand, right);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise COMPARISON with params:" + params, e);
    }
  }

  public Context parseContext(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 1) {
        throw new IllegalArgumentException("Expecting 1 parameters but found " + tokens.length);
      }
      ContextOperand operand = ContextOperand.fromString(trimToken(tokens[0]));
      return new Context(operand);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise CONTEXT with params:" + params, e);
    }
  }
  
  public Fail parseFail(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 1) {
        throw new IllegalArgumentException("Expecting 1 parameter but found " + tokens.length);
      }
      IExpression valueExpr = getExpression(tokens[0]);
      return new Fail(valueExpr);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise Fail with params:" + params, e);
    }
  }
  
  public Flag parseFlag(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 2) {
        throw new IllegalArgumentException("Expecting 2 parameters but found " + tokens.length);
      }
      FlagOperand operand = FlagOperand.fromString(trimToken(tokens[0]));
      IExpression flagExpr = getExpression(tokens[1]);
      return new Flag(operand, flagExpr);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise FLAG with params:" + params, e);
    }
  }
  
  public If parseIf(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      IExpression condition = getExpression(tokens[0]);
      IExpression trueResult = getExpression(tokens[1]);
      IExpression falseResult = getExpression(tokens[2]);
      return new If(condition, trueResult, falseResult);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise IF with params:" + params, e);
    }
  }

  public Logic parseLogic(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      IExpression left = getExpression(tokens[0]);
      LogicOperand operand = LogicOperand.fromString(trimToken(tokens[1]));
      IExpression right = getExpression(tokens[2]);
      return new Logic(left, operand, right);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise LOGIC with params:" + params, e);
    }
  }
  
  public Present parsePresent(String params) {
    Objects.requireNonNull(params, "params may not be null");
    String[] tokens = tokenise(params);
    if(tokens.length != 1) {
      throw new IllegalArgumentException("expected a single acronym");
    }
    String acronym = trimToken(tokens[0]);
    acronym = Convert.toUnquotedString(acronym);
    return new Present(acronym);
  }
  
  public Quantity parseQuantity(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 3) {
        throw new IllegalArgumentException("Expecting 3 parameters but found " + tokens.length);
      }
      QuantityOperand operand = QuantityOperand.fromString( trimToken(tokens[0]) );
      String acronym = trimToken( tokens[1] );
      String sourceIdToken = trimToken( tokens[2] );
      //note: can be "null" (now case-insensive as of 0.6.3), or from 0.6.4 onwards we use "any"
      final SourceId _sourceId = SourceId.fromString( sourceIdToken );      
      return new Quantity(operand, acronym, _sourceId);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise QUANTITY with params:" + params, e);
    }
  }

  public Rnd parseRnd(String params) {
    Objects.requireNonNull(params, "params may not be null");
    try {
      String[] tokens = tokenise(params);
      if(tokens.length != 2) {
        throw new IllegalArgumentException("Expecting 2 parameters but found " + tokens.length);
      }
      int min = Integer.parseInt(tokens[0]);
      int max = Integer.parseInt(tokens[1]);
      return new Rnd(min, max);
    } catch(IllegalArgumentException illArgs) {
      throw illArgs;
    } catch(Exception e) {
      throw new RuntimeException("Unable to initialise RND with params:" + params, e);
    }
  }

  public Value parseValue(String params) {
    
    Objects.requireNonNull(params, "params may not be null");
    String[] tokens = tokenise(params);
    if(tokens.length != 1) {
     throw new IllegalArgumentException("VALUE requires one and only one parameter"); 
    }
    String token = trimToken(tokens[0]);
    Object value = Convert.toObject(token);
    return new Value(value);
  }
}
