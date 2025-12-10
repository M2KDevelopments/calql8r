import 'dart:io';
import 'dart:math';

// --- 1. Operator and Function Definitions ---

// Define custom function types for clarity
typedef BinaryFunction = double Function(double a, double b);
typedef UnaryFunction = double Function(double a);

/// Represents an operator or function with its properties.
class OpFuncDef {
  final String name;
  final int precedence;
  final bool isLeftAssociative;
  final int arity; // 1 for unary, 2 for binary
  final Function function;

  OpFuncDef(this.name, this.precedence, this.isLeftAssociative, this.arity, this.function);
}

// Global static map for all known operators and functions
final Map<String, OpFuncDef> definitions = {};

// --- 2. Function Implementations ---

// Binary Functions
double _add(double a, double b) => a + b;
double _subtract(double a, double b) => a - b;
double _multiply(double a, double b) => a * b;
double _divide(double a, double b) => a / b;
double _power(double a, double b) => pow(a, b).toDouble();

// Custom Root: a r b means b-th root of a. (Root degree is b)
double _root(double a, double b) {
  if (a < 0 && b % 2.0 == 0.0) {
    throw ArgumentError('Cannot take even root of a negative number.');
  }
  return pow(a, 1.0 / b).toDouble();
}

// Unary Functions
double _sin(double a) => sin(a);
double _sinh(double a) => asin(a);
double _cos(double a) => cos(a);
double _cosh(double a) => acos(a);
double _tan(double a) => tan(a);
double _tanh(double a) => atan(a);
double _ln(double a) => log(a); // Natural log
double _log10(double a) => log(a) / log(10); // Base 10 log

// Unary Post-fix Factorial
double _factorial(double a) {
  if (a < 0 || a % 1.0 != 0.0) {
    throw ArgumentError('Factorial only defined for non-negative integers.');
  }
  if (a == 0) return 1.0;
  
  int res = 1;
  for (int i = 1; i <= a.toInt(); i++) {
    res *= i;
  }
  return res.toDouble();
}

/// Initializes the static function definitions map.
void _initializeDefinitions() {
  if (definitions.isNotEmpty) return;

  // Precedence: Higher number binds tighter
  
  // Postfix Unary (Highest Precedence)
  definitions['!'] = OpFuncDef('!', 6, true, 1, _factorial); 

  // Prefix Unary Functions (Right Associative precedence 5)
  definitions['S'] = OpFuncDef('S', 5, false, 1, _sin);
  definitions['s'] = OpFuncDef('s', 5, false, 1, _sinh);
  definitions['C'] = OpFuncDef('C', 5, false, 1, _cos);
  definitions['c'] = OpFuncDef('c', 5, false, 1, _cosh);
  definitions['T'] = OpFuncDef('T', 5, false, 1, _tan);
  definitions['t'] = OpFuncDef('t', 5, false, 1, _tanh);
  definitions['l'] = OpFuncDef('l', 5, false, 1, _ln);    // Ln
  definitions['L'] = OpFuncDef('L', 5, false, 1, _log10); // Log10

  // Binary Operators
  definitions['^'] = OpFuncDef('^', 4, false, 2, _power);  // Right Associative
  definitions['r'] = OpFuncDef('r', 4, true, 2, _root);    // Root (Left Associative)

  definitions['*'] = OpFuncDef('*', 3, true, 2, _multiply);
  definitions['/'] = OpFuncDef('/', 3, true, 2, _divide);
  
  definitions['+'] = OpFuncDef('+', 2, true, 2, _add);
  definitions['&'] = OpFuncDef('-', 2, true, 2, _subtract); // Use '&' temporarily for binary subtract
}

// --- 3. Lexer/Tokenizer ---

/// Converts the raw input string into a list of tokens.
List<String> _tokenize(String expression) {
  // 1. Prepare separators for splitting.
  // We use a list of all symbols and functions
  const List<String> separators = [
    '(', ')', '+', '-', '*', '/', '^', '!', 'r', 
    'S', 's', 'C', 'c', 'T', 't', 'l', 'L', 'p'
  ];

  // 2. Insert spaces around all separators to facilitate splitting,
  //    but first, replace binary minus with a placeholder ('&') 
  //    to distinguish it from unary minus.
  String processed = expression.replaceAll(' ', '');
  
  // Simple pass to separate all tokens
  for (final sep in separators) {
    processed = processed.replaceAll(sep, ' $sep ');
  }
  
  // Clean up multiple spaces and trim, then split
  final tokens = processed
      .split(RegExp(r'\s+'))
      .where((s) => s.isNotEmpty)
      .toList();

  // 3. Handle Unary Minus and Binary Minus
  for (int i = 0; i < tokens.length; i++) {
    if (tokens[i] == '-') {
      // Check if the preceding token is an operator, function, or '('.
      // If it is, this '-' is unary. Otherwise, it's binary.
      bool isPrecedingTokenOperand = i > 0 && 
          (double.tryParse(tokens[i - 1]) != null || tokens[i - 1] == 'p' || tokens[i - 1] == '!' || tokens[i-1] == ')');

      if (i == 0 || !isPrecedingTokenOperand) {
        // Unary minus: merge it with the following token (e.g., -5)
        if (i + 1 < tokens.length) {
          tokens[i + 1] = tokens[i] + tokens[i + 1];
          tokens.removeAt(i);
          i--; // Adjust index after removal
        }
      } else {
        // Binary minus: replace with placeholder for RPN evaluation
        tokens[i] = '&'; 
      }
    }
  }
  
  return tokens;
}

// --- 4. Shunting-Yard Algorithm (Infix to RPN) ---

/// Converts a list of infix tokens to a list of RPN tokens.
List<String> _infixToRpn(List<String> infixTokens) {
  final outputQueue = <String>[];
  final operatorStack = <String>[];
  
  for (final token in infixTokens) {
    // 1. If the token is a number or constant 'p', add it to the output queue.
    if (double.tryParse(token) != null || token == 'p') {
      outputQueue.add(token);
    }
    // 2. If the token is a function or '('
    else if (definitions.containsKey(token) && definitions[token]!.arity == 1 && token != '!' || token == '(') {
      operatorStack.add(token);
    }
    // 3. If the token is a binary operator ('+' is represented by '&')
    else if (definitions.containsKey(token) && definitions[token]!.arity == 2) {
      final OpFuncDef currentDef = definitions[token]!;
      
      while (operatorStack.isNotEmpty) {
        final topToken = operatorStack.last;
        if (topToken == '(') break;

        final OpFuncDef? topDef = definitions[topToken];
        if (topDef == null) break; // Should not happen if logic is correct
        
        // Check precedence and associativity
        if ((currentDef.isLeftAssociative && currentDef.precedence <= topDef.precedence) ||
            (!currentDef.isLeftAssociative && currentDef.precedence < topDef.precedence)) {
          outputQueue.add(operatorStack.removeLast());
        } else {
          break;
        }
      }
      operatorStack.add(token);
    }
    // 4. If the token is a postfix operator '!'
    else if (token == '!') {
         outputQueue.add(token);
    }
    // 5. If the token is ')'
    else if (token == ')') {
      while (operatorStack.isNotEmpty && operatorStack.last != '(') {
        outputQueue.add(operatorStack.removeLast());
      }
      if (operatorStack.isEmpty) {
        throw ArgumentError('Mismatched parentheses in expression.');
      }
      operatorStack.removeLast(); // Pop the '('
      
      // If there is a function token at the top of the stack, pop it.
      if (operatorStack.isNotEmpty && definitions.containsKey(operatorStack.last)) {
        outputQueue.add(operatorStack.removeLast());
      }
    }
    else {
      throw ArgumentError('Invalid token found: $token');
    }
  }
  
  // 6. Pop remaining operators from stack to RPN output.
  while (operatorStack.isNotEmpty) {
    final token = operatorStack.removeLast();
    if (token == '(') {
      throw ArgumentError('Mismatched parentheses in expression.');
    }
    outputQueue.add(token);
  }
  
  return outputQueue;
}

// --- 5. RPN Evaluation ---

/// Evaluates a list of RPN tokens.
double _evaluateRpn(List<String> rpnTokens) {
  final valueStack = <double>[];

  for (final token in rpnTokens) {
    // 1. If the token is a number or 'p', push the value onto the stack.
    if (double.tryParse(token) != null) {
      valueStack.add(double.parse(token));
    } else if (token == 'p') {
      valueStack.add(pi);
    }
    // 2. If the token is a function or operator.
    else if (definitions.containsKey(token)) {
      final def = definitions[token]!;
      
      if (def.arity == 2) { // Binary
        if (valueStack.length < 2) throw ArgumentError('Binary operator \'${def.name}\' requires two operands.');
        double b = valueStack.removeLast();
        double a = valueStack.removeLast();
        valueStack.add((def.function as BinaryFunction)(a, b));
      } else if (def.arity == 1) { // Unary
        if (valueStack.isEmpty) throw ArgumentError('Unary operator \'${def.name}\' requires one operand.');
        double a = valueStack.removeLast();
        valueStack.add((def.function as UnaryFunction)(a));
      }
    }
    else {
      throw ArgumentError('Unknown token during evaluation: $token');
    }
  }

  if (valueStack.length != 1) {
    throw ArgumentError('Invalid RPN expression (too many/few operands).');
  }

  return valueStack.first;
}

// --- 6. Main Calculation Flow ---

/// Public function to calculate the result of an infix expression.
double calculate(String expression) {
  if (expression.trim().isEmpty) return double.nan;
  
  _initializeDefinitions(); // Ensure definitions are loaded

  final tokens = _tokenize(expression);
  final rpn = _infixToRpn(tokens);
  return _evaluateRpn(rpn);
}

// --- 7. Main CLI Loop ---

void main() {
  _initializeDefinitions(); // Initialize definitions once

  print('--- Dart CLI Calculator (Infix Mode) ---');
  print('Supported Operations:');
  print('  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)');
  print('  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S30)');
  print('  Unary (Postfix): ! (Factorial, e.g., 6!)');
  print('  Constant: p (PI)');
  print('\nNOTE: Use explicit multiplication (e.g., 2*(3) is correct).');
  print('Type \'exit\' or \'quit\' to end.\n');

  while (true) {
    stdout.write('Expression: ');
    final input = stdin.readLineSync()?.trim();

    if (input == null || input.isEmpty || input.toLowerCase() == 'exit' || input.toLowerCase() == 'quit') {
      break;
    }

    try {
      final result = calculate(input);
      print('Result: **${result.toStringAsFixed(10)}**\n');
    } on ArgumentError catch (e) {
      print('Error: Invalid expression. ${e.message}\n');
    } catch (e) {
      print('An unexpected error occurred: $e\n');
    }
  }

  print('Exiting calculator. Goodbye!');
}