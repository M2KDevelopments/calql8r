import 'dart:io';
import 'dart:math';

void main() {
  print("Dart CLI Scientific Calculator (type 'exit' to quit)");

  while (true) {
    stdout.write("> ");
    String? input = stdin.readLineSync();

    if (input == null) continue;
    if (input.trim().toLowerCase() == "exit") break;

    try {
      var parser = ExpressionParser(input);
      double result = parser.parseExpression();
      print("= $result");
    } catch (e) {
      print("Error: $e");
    }
  }
}

// ======================
//     PARSER CLASS
// ======================
class ExpressionParser {
  String text;
  int pos = 0;

  ExpressionParser(this.text) {
    text = text.replaceAll(" ", "");
  }

  String peek() => pos < text.length ? text[pos] : '\0';
  String next() => pos < text.length ? text[pos++] : '\0';

  bool eat(String c) {
    if (peek() == c) {
      pos++;
      return true;
    }
    return false;
  }

  // -------------------------
  // Grammar: expression → term ((+|-) term)*
  // -------------------------
  double parseExpression() {
    double value = parseTerm();

    while (true) {
      if (eat('+')) {
        value += parseTerm();
      } else if (eat('-')) {
        value -= parseTerm();
      } else {
        return value;
      }
    }
  }

  // -------------------------
  // term → factor ((*|/) factor)*
  // -------------------------
  double parseTerm() {
    double value = parseFactor();

    while (true) {
      if (eat('*')) {
        value *= parseFactor();
      } else if (eat('/')) {
        value /= parseFactor();
      } else {
        return value;
      }
    }
  }

  // -------------------------
  // factor → unary (^ unary | r unary)*
  // -------------------------
  double parseFactor() {
    double value = parseUnary();

    while (true) {
      if (eat('^')) {
        value = pow(value, parseUnary()).toDouble();
      } else if (eat('r')) {
        // a r b = b^(1/a)
        double b = parseUnary();
        value = pow(b, 1.0 / value).toDouble();
      } else {
        return value;
      }
    }
  }

  // -------------------------
  // unary → (+|-) unary | primary
  // -------------------------
  double parseUnary() {
    if (eat('+')) return parseUnary();
    if (eat('-')) return -parseUnary();
    return parsePrimary();
  }

  // -------------------------
  // primary → number | function primary | '(' expression ')' | factorial
  // -------------------------
  double parsePrimary() {
    String c = peek();

    // Function letter
    if (RegExp(r"[A-Za-z]").hasMatch(c)) {
      String f = readFunction();
      double arg = parsePrimary();
      return applyFunction(f, arg);
    }

    // Parentheses
    if (eat('(')) {
      double val = parseExpression();
      if (!eat(')')) throw "Missing closing parenthesis";
      if (eat('!')) return factorial(val);
      return val;
    }

    // Number or constant p
    double number = parseNumber();
    if (eat('!')) return factorial(number);

    return number;
  }

  // Read S, C, T, l, L etc.
  String readFunction() {
    String f = "";
    while (RegExp(r"[A-Za-z]").hasMatch(peek())) {
      f += next();
    }
    return f;
  }

  // Numbers + π
  double parseNumber() {
    if (peek() == 'p') {
      next();
      return pi;
    }

    String number = "";
    while (RegExp(r"[0-9.]").hasMatch(peek())) {
      number += next();
    }

    if (number.isEmpty) throw "Number expected";

    return double.parse(number);
  }

  double applyFunction(String f, double x) {
    switch (f) {
      case "S":
        return sin(x);
      case "s":
        return asin(x);
      case "C":
        return cos(x);
      case "c":
        return acos(x);
      case "T":
        return tan(x);
      case "h":
        return atan(x);
      case "l":
        return log(x);
      case "L":
        return log(x) / ln10;
      case "r":
        return sqrt(x); // default sqrt
      default:
        throw "Unknown function: $f";
    }
  }

  double factorial(double n) {
    if (n < 0) throw "Factorial of negative number";

    int k = n.toInt();
    double r = 1;

    for (int i = 2; i <= k; i++) r *= i;
    return r;
  }
}
