import 'dart:io';
import 'dart:math';

double factorial(double n) {
  if (n < 0) {
    throw ArgumentError('Factorial not defined for negative numbers');
  }
  if (n == 0 || n == 1) {
    return 1;
  }
  
  double result = 1;
  for (int i = 2; i <= n.toInt(); i++) {
    result *= i;
  }
  return result;
}

double evaluateExpression(String input) {
  String expr = input.replaceAll(RegExp(r'\s+'), '');
  
  // Replace p with pi
  expr = expr.replaceAll('p', pi.toString());
  
  // Handle factorials (n!)
  expr = expr.replaceAllMapped(
    RegExp(r'(\d+\.?\d*)!'),
    (match) {
      double num = double.parse(match.group(1)!);
      return factorial(num).toString();
    },
  );
  
  // Handle two-value functions (aFb format)
  
  // Pow (^): a^b
  expr = expr.replaceAllMapped(
    RegExp(r'(\d+\.?\d*)\^(\d+\.?\d*)'),
    (match) {
      double a = double.parse(match.group(1)!);
      double b = double.parse(match.group(2)!);
      return pow(a, b).toString();
    },
  );
  
  // Root (r): arb means b-th root of a
  expr = expr.replaceAllMapped(
    RegExp(r'(\d+\.?\d*)r(\d+\.?\d*)'),
    (match) {
      double a = double.parse(match.group(1)!);
      double b = double.parse(match.group(2)!);
      return pow(a, 1.0 / b).toString();
    },
  );
  
  // Handle one-value functions (Fn format)
  
  // Sin - S
  expr = expr.replaceAllMapped(
    RegExp(r'S(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return sin(n).toString();
    },
  );
  
  // SinH - s
  expr = expr.replaceAllMapped(
    RegExp(r's(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return sinh(n).toString();
    },
  );
  
  // Cos - C
  expr = expr.replaceAllMapped(
    RegExp(r'C(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return cos(n).toString();
    },
  );
  
  // CosH - c
  expr = expr.replaceAllMapped(
    RegExp(r'c(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return cosh(n).toString();
    },
  );
  
  // Tan - T
  expr = expr.replaceAllMapped(
    RegExp(r'T(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return tan(n).toString();
    },
  );
  
  // Tanh - t
  expr = expr.replaceAllMapped(
    RegExp(r't(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return tanh(n).toString();
    },
  );
  
  // Ln - l
  expr = expr.replaceAllMapped(
    RegExp(r'l(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return log(n).toString();
    },
  );
  
  // Log (base 10) - L
  expr = expr.replaceAllMapped(
    RegExp(r'L(\d+\.?\d*)'),
    (match) {
      double n = double.parse(match.group(1)!);
      return (log(n) / log(10)).toString();
    },
  );
  
  // Evaluate the final expression
  return Parser(expr).parseExpression();
}

double sinh(double x) {
  return (exp(x) - exp(-x)) / 2;
}

double cosh(double x) {
  return (exp(x) + exp(-x)) / 2;
}

double tanh(double x) {
  return sinh(x) / cosh(x);
}

class Parser {
  String expr;
  int pos = 0;
  
  Parser(this.expr);
  
  String? currentChar() {
    return pos < expr.length ? expr[pos] : null;
  }
  
  void advance() {
    pos++;
  }
  
  void skipWhitespace() {
    while (currentChar() != null && currentChar() == ' ') {
      advance();
    }
  }
  
  double parseExpression() {
    double result = parseTerm();
    
    while (true) {
      skipWhitespace();
      String? ch = currentChar();
      
      if (ch == '+') {
        advance();
        result += parseTerm();
      } else if (ch == '-') {
        advance();
        result -= parseTerm();
      } else {
        break;
      }
    }
    
    return result;
  }
  
  double parseTerm() {
    double result = parseFactor();
    
    while (true) {
      skipWhitespace();
      String? ch = currentChar();
      
      if (ch == '*') {
        advance();
        result *= parseFactor();
      } else if (ch == '/') {
        advance();
        result /= parseFactor();
      } else {
        break;
      }
    }
    
    return result;
  }
  
  double parseFactor() {
    skipWhitespace();
    
    // Handle negative numbers
    if (currentChar() == '-') {
      advance();
      return -parseFactor();
    }
    
    // Handle positive sign
    if (currentChar() == '+') {
      advance();
      return parseFactor();
    }
    
    // Handle parentheses
    if (currentChar() == '(') {
      advance();
      double result = parseExpression();
      skipWhitespace();
      if (currentChar() == ')') {
        advance();
      } else {
        throw FormatException("Expected ')'");
      }
      return result;
    }
    
    // Handle numbers
    return parseNumber();
  }
  
  double parseNumber() {
    skipWhitespace();
    int start = pos;
    
    while (currentChar() != null && 
           (RegExp(r'\d').hasMatch(currentChar()!) || currentChar() == '.')) {
      advance();
    }
    
    if (start == pos) {
      throw FormatException('Expected number at position $pos');
    }
    
    return double.parse(expr.substring(start, pos));
  }
}

void main() {
  print('=' * 50);
  print('CLI Calculator');
  print('=' * 50);
  print('\nOperators:');
  print('  S - Sin      s - SinH');
  print('  C - Cos      c - CosH');
  print('  T - Tan      t - TanH');
  print('  ^ - Power    r - Root (arb = b-th root of a)');
  print('  l - Ln       L - Log (base 10)');
  print('  ! - Factorial');
  print('  p - Pi (3.14159...)');
  print('\nExamples:');
  print('  1+2');
  print('  2+5^4');
  print('  2*(2+25)');
  print('  6!');
  print('  2+4^2');
  print('  8r3 (cube root of 8)');
  print('  L100 (log base 10 of 100)');
  print('  S1.57 (sin of 1.57)');
  print('\nType \'quit\' or \'exit\' to exit\n');
  print('=' * 50);
  
  while (true) {
    try {
      stdout.write('\nEnter expression: ');
      String? input = stdin.readLineSync();
      
      if (input == null) {
        break;
      }
      
      input = input.trim();
      
      // Check for exit commands
      if (input.toLowerCase() == 'quit' || 
          input.toLowerCase() == 'exit' || 
          input.toLowerCase() == 'q') {
        print('Goodbye!');
        break;
      }
      
      // Skip empty input
      if (input.isEmpty) {
        continue;
      }
      
      // Evaluate and display result
      double result = evaluateExpression(input);
      print('Result: $result');
      
    } on ArgumentError catch (e) {
      print('Error: ${e.message}');
    } on FormatException catch (e) {
      print('Error: ${e.message}');
    } catch (e) {
      print('Unexpected error: $e');
    }
  }
}