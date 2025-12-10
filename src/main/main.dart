import 'dart:math';

// Constamts
const DECIMAL_POINT = '.';
const OPERATOR_ADD = '+';
const OPERATOR_SUBSTRACT = '-';
const OPERATOR_MULTPILY = '*';
const OPERATOR_DIVIDE = '/';
const OPERATOR_LOGx = 'l';
const OPERATOR_POW = '^';
const OPERATOR_ROOT = 'r';
const OPERATOR_SIN = 'S';
const OPERATOR_SINH = 's';
const OPERATOR_COS = 'C';
const OPERATOR_COSH = 'c';
const OPERATOR_TAN = 'T';
const OPERATOR_TANH = 't';
const OPERATOR_LOG10 = 'L';
const OPERATOR_LN = 'E';
const OPERATOR_FACTORIAL = '!';

const FACTORIAL = '!';
const PERMUTATIONS = 'Y';
const COMBINATIONS = 'Z';

enum EnumFunctionValueDirection { LEFT, RIGHT }

List<dynamic>? construct_numbers_from_string_of_integers(
    List<dynamic> expression) {
  int start = -1;
  int end = 0;
  for (int i = 0; i < expression.length; i++) {
    const numbers = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];

    if (numbers.contains(expression[i])) {
      if (start == -1) start = i;
      end = i;
      if ((end == expression.length - 1) && start != -1) {
        try {
          var number = expression.getRange(start, end + 1).join("");
          var value = int.parse(number);
          expression[start] = value;
          for (int j = start + 1; j <= end; j++) expression[j] = '';
        } catch (e) {
          return null;
        }
      }
    } else {
      if (start != -1) {
        try {
          var number = expression.getRange(start, end + 1).join("");
          var value = int.parse(number);
          expression[start] = value;
          for (int j = start + 1; j <= end; j++) expression[j] = '';
        } catch (e) {
          return null;
        }
        start = -1;
      }
    }
  }

  expression.removeWhere((element) => element == '');
  return expression;
}

List<dynamic>? construct_decimal_numbers(List<dynamic> expression) {
  int i = 0;
  while (i < expression.length) {
    if (expression[i] == DECIMAL_POINT) {
      if (i == 0 && (i == expression.length - 1)) return null;
      try {
        var number = "${expression[i - 1]}.${expression[i + 1]}";
        expression[i - 1] = double.parse(number);
        expression.removeAt(i);
        expression.removeAt(i);
      } catch (e) {
        return null;
      }
    } else {
      i++;
    }
  }
  return expression;
}

List<dynamic>? convert_negative_numbers(List<dynamic> expression) {
  for (int i = 0; i < expression.length; i++) {
    if (expression[i] == OPERATOR_SUBSTRACT) {
      if (i == (expression.length - 1)) return null;

      if (expression[i + 1] == OPERATOR_SUBSTRACT) {
        // double negative
        expression[i] = OPERATOR_ADD;
        expression[i + 1] = '';
      } else if (i == 0 && expression[i + 1] is num) {
        expression[i] = '';
        expression[i + 1] *= -1;
      } else if (i > 0 &&
          (expression[i - 1] is num == false) &&
          expression[i + 1] is num) {
        expression[i] = '';
        expression[i + 1] *= -1;
      }
    }
  }
  expression.removeWhere((element) => element == '');
  return expression;
}

List<dynamic>? calculate_1_value_expression(
    List<dynamic> expression,
    String operation_symbol,
    EnumFunctionValueDirection direction,
    double? Function(double) calculation_function) {
  int i = 0;
  while (i < expression.length) {
    if (expression[i] == operation_symbol) {
      if (i == 0 && direction == EnumFunctionValueDirection.LEFT) {
        return null;
      }
      if (i == (expression.length - 1) &&
          direction == EnumFunctionValueDirection.RIGHT) {
        return null;
      }
      if (expression[i - 1] is num == false &&
          direction == EnumFunctionValueDirection.LEFT) {
        return null;
      }
      if (expression[i + 1] is num == false &&
          direction == EnumFunctionValueDirection.RIGHT) {
        return null;
      }

      double? result;
      if (direction == EnumFunctionValueDirection.LEFT) {
        result = calculation_function(double.parse(expression[i - 1].toString()));
        if (result == null) return null;
        // Remove unnecessary elements and update value
        expression[i - 1] = result;
        expression.removeAt(i);
        i = i - 1;
      } else if (direction == EnumFunctionValueDirection.RIGHT) {
        result = calculation_function(double.parse(expression[i + 1].toString()));
        if (result == null) return null;
        // Remove unnecessary elements and update value
        expression[i] = result;
        expression.removeAt(i + 1);
      } else {
        i++;
      }
    } else {
      i++;
    }
  }

  return expression;
}

List<dynamic>? calculate_2_value_expressions(
    List<dynamic> expression,
    String operation_symbol,
    double? Function(double, double) calculation_function) {
  int i = 0;
  while (i < expression.length) {
    if (expression[i] == operation_symbol) {
      var prev_num = expression[i - 1];
      var next_num = expression[i + 1];
      var result = calculation_function(double.parse(prev_num.toString()),
            double.parse(next_num.toString()));
        if (result == null) return null;
        // Remove unnecessary elements and update value
        expression[i - 1] = result;
        expression.removeAt(i);
        expression.removeAt(i);
        i = i - 1;
    } else {
      i++;
    }
  }

  return expression;
}

double calculate_factorial(double number) {
  if (number < 0) return 0;
  if (number == 0) return 1;
  if (number == 1) return 1;
  if (number == 2) return 2;
  int total = 1;
  for (int n = 1; n <= number; n++) total *= n;
  return total.toDouble();
}

double? calculate_permutation(double n, double r) {
  if (n < r || n < 0 || r < 0) return null;
  return calculate_factorial(n) / calculate_factorial(n - r);
}

double? calculate_combinations(double n, double r) {
  if (n < r || n < 0 || r < 0) return null;
  return calculate_factorial(n) /
      (calculate_factorial(r) * calculate_factorial(n - r));
}

double? calculate_math(List<dynamic> expr) {
  try {
    // factorial and nPr and nCr
    List<dynamic>? expression = calculate_1_value_expression(
        expr, OPERATOR_FACTORIAL, EnumFunctionValueDirection.LEFT, (number) {
      var value = calculate_factorial(number);
      if (value == 0) return null;
      return value;
    });
    if (expression == null) return null;

    expression = calculate_2_value_expressions(
        expression, PERMUTATIONS, (n, r) => calculate_permutation(n, r));
    if (expression == null) return null;

    expression = calculate_2_value_expressions(
        expression, COMBINATIONS, (n, r) => calculate_combinations(n, r));
    if (expression == null) return null;

    // calculate trigonometry

    expression = calculate_1_value_expression(expression, OPERATOR_SIN,
        EnumFunctionValueDirection.RIGHT, (number) => sin(number));
    if (expression == null) return null;

    expression = calculate_1_value_expression(expression, OPERATOR_SINH,
        EnumFunctionValueDirection.RIGHT, (number) => asin(number));
    if (expression == null) return null;

    expression = calculate_1_value_expression(expression, OPERATOR_COS,
        EnumFunctionValueDirection.RIGHT, (number) => cos(number));
    if (expression == null) return null;

    expression = calculate_1_value_expression(expression, OPERATOR_COSH,
        EnumFunctionValueDirection.RIGHT, (number) => acos(number));
    if (expression == null) return null;

    expression = calculate_1_value_expression(expression, OPERATOR_TAN,
        EnumFunctionValueDirection.RIGHT, (number) => tan(number));
    if (expression == null) return null;

    expression = calculate_1_value_expression(expression, OPERATOR_TANH,
        EnumFunctionValueDirection.RIGHT, (number) => atan(number));
    if (expression == null) return null;

    // calculate logarithms
    expression = calculate_1_value_expression(expression, OPERATOR_LOG10,
        EnumFunctionValueDirection.RIGHT, (number) => log(number) / log(10));
    if (expression == null) return null;

    expression = calculate_1_value_expression(expression, OPERATOR_LN,
        EnumFunctionValueDirection.RIGHT, (number) => log(number));
    if (expression == null) return null;

    expression = calculate_2_value_expressions(
        expression, OPERATOR_LOGx, (a, b) => log(a) / log(b));
    if (expression == null) return null;

    // calculate exponents and roots
    expression = calculate_2_value_expressions(
        expression, OPERATOR_POW, (a, b) => pow(a, b).toDouble());
    if (expression == null) return null;

    expression = calculate_2_value_expressions(
        expression, OPERATOR_ROOT, (a, b) => pow(b, 1 / a).toDouble());
    if (expression == null) return null;

    // calculate basic arithmitic
    expression = calculate_2_value_expressions(
        expression, OPERATOR_DIVIDE, (a, b) => a / b);
    if (expression == null) return null;

    expression = calculate_2_value_expressions(
        expression, OPERATOR_MULTPILY, (a, b) => a * b);
    if (expression == null) return null;

    expression = calculate_2_value_expressions(
        expression, OPERATOR_SUBSTRACT, (a, b) => a - b);
    if (expression == null) return null;

    expression = calculate_2_value_expressions(
        expression, OPERATOR_ADD, (a, b) => a + b);
    if (expression == null) return null;

    if (expression.length != 1) return null;
    if (expression[0] is num == false) return null;

    double value = double.parse(expression[0].toString());
    return value;
  } catch (e) {
    return null;
  }
}

List<dynamic>? calculate_innermost_brackets(
    List<dynamic> expression, double? Function(List<dynamic>) calculate_math) {
  int last_open_bracket = -1;
  int first_close_bracket = -1;
  int count_open_bracket = 0;
  int count_close_bracket = 0;
  for (int i = 0; i < expression.length; i++) {
    if (expression[i] == '(') {
      last_open_bracket = i;
      count_open_bracket += 1;
    } else if (expression[i] == ')') {
      count_close_bracket += 1;
      if (first_close_bracket == -1) {
        first_close_bracket = i;
      }
    }

    // Syntax error
    if (count_close_bracket > count_open_bracket) return null;

    // when the number of open brackets and closing brackets match.
    // 'last_open_bracket' is the start and 'first_close_bracket' is the end. for the calculation
    if (count_open_bracket == count_close_bracket &&
        (first_close_bracket != -1)) {
      var start = last_open_bracket + 1;
      var end = first_close_bracket;
      var bracket_expression = expression.getRange(start, end).toList();
      var value = calculate_math(bracket_expression);
      if (value == null) return null;
      expression[last_open_bracket] = value;

      // remove all elements from last_open_bracket to first_close_bracket
      for (int j = last_open_bracket + 1; j <= first_close_bracket; j++) {
        expression.removeAt(last_open_bracket+1);
      }
      return expression;
    }
  }

  return expression;
}

void main(List<String> args) {

  if (args.length < 1) return print("PLEASE ADD AN EXPRESSION TO CALCULATE");
  List<dynamic>? expression = [];

  // construct expression for arguments e.g 1+1 +2 /4 *4
  // whitespaces are automatically handled by joining each argument
  for (var arg in args) {
    for (var c in arg.split("")) {
      if(c != ' '){
        expression.add(c);
      }   
    }
  }

  // calculate integer numbers
  expression = construct_numbers_from_string_of_integers(expression);
  if (expression == null) return print("INVALID NUMBER FORMAT");

  // calculate decimal numbers
  expression = construct_decimal_numbers(expression);
  if (expression == null) return print("INVALID DECIMAL NUMBER FORMAT");

  // replace all PI symbols with value
  for (int i = 0; i < expression.length; i++) {
    if (['π', 'PI', 'pi', 'p'].contains(expression[i])) expression[i] = pi;
  }

  // convert negative numbers
  expression = convert_negative_numbers(expression);
  if (expression == null) return print("INVALID NEGATIVE NUMBER FORMAT");

  // Calculate inner bracket expressions
  do {
    expression = calculate_innermost_brackets(expression!, calculate_math);
    if (expression == null) return print("MATH ERROR");
  } while (expression.contains("("));

  var value = calculate_math(expression);
  if (value == null) return print("MATH ERROR");

  print(value);
}
