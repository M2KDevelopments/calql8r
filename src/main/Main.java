package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    private static final String DECIMAL_POINT = ".";
    private static final String OPERATOR_ADD = "+";
    private static final String OPERATOR_SUBSTRACT = "-";
    private static final String OPERATOR_MULTPILY = "*";
    private static final String OPERATOR_DIVIDE = "/";
    private static final String OPERATOR_LOGx = "l";
    private static final String OPERATOR_POW = "^";
    private static final String OPERATOR_ROOT = "r";
    private static final String OPERATOR_SIN = "S";
    private static final String OPERATOR_SINH = "s";
    private static final String OPERATOR_COS = "C";
    private static final String OPERATOR_COSH = "c";
    private static final String OPERATOR_TAN = "T";
    private static final String OPERATOR_TANH = "t";
    private static final String OPERATOR_LOG10 = "L";
    private static final String OPERATOR_LN = "E";
    private static final String OPERATOR_FACTORIAL = "!";

    private static final String PERMUTATIONS = "Y";
    private static final String COMBINATIONS = "Z";

    private enum EnumFunctionValueDirection {LEFT, RIGHT}

    private static final ArrayList ERROR = new ArrayList();

    private interface ICalculateFunctionOneValue {
        double calculation_function(double num);
    }

    private interface ICalculateFunctionTwoValues {
        double calculation_function(double num1, double num2);
    }

    private interface ICalculations {
        double calculate_math(ArrayList expression);
    }

    private static ArrayList construct_numbers_from_string_of_integers(ArrayList expression) {
        int start = -1;
        int end = 0;
        String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        for (int i = 0; i < expression.size(); i++) {
            if (Arrays.binarySearch(numbers, expression.get(i).toString()) >= 0) {
                if (start == -1) start = i;
                end = i;
                if ((end == expression.size() - 1) && start != -1) {
                    try {
                        var number = String.join("", expression.subList(start, end + 1));
                        var value = Integer.parseInt(number);
                        expression.set(start, value);
                        for (int j = start + 1; j <= end; j++) expression.set(j, "");
                    } catch (Exception e) {
                        return ERROR;
                    }
                }
            } else {
                if (start != -1) {
                    try {
                        var number = String.join("", expression.subList(start, end + 1));
                        var value = Integer.parseInt(number);
                        expression.set(start, value);
                        for (int j = start + 1; j <= end; j++) expression.set(j, "");
                    } catch (Exception e) {
                        return ERROR;
                    }
                    start = -1;
                }
            }
        }

        expression.removeIf(n -> n == "");
        return expression;
    }

    private static ArrayList construct_decimal_numbers(ArrayList expression) {
        int i = 0;
        while (i < expression.size()) {
            if (Objects.equals(expression.get(i).toString(), DECIMAL_POINT)) {
                if (i == 0 && (i == expression.size() - 1)) return null;
                try {
                    var number = expression.get(i - 1) + "." + expression.get(i + 1);
                    expression.set(i - 1, Double.parseDouble(number));
                    expression.remove(i);
                    expression.remove(i);
                } catch (Exception e) {
                    return ERROR;
                }
            } else {
                i++;
            }
        }
        return expression;
    }

    private static ArrayList convert_negative_numbers(ArrayList expression) {
        for (int i = 0; i < expression.size(); i++) {
            if (Objects.equals(expression.get(i).toString(), OPERATOR_SUBSTRACT)) {
                if (i == (expression.size() - 1)) return null;

                if (Objects.equals(expression.get(i + 1).toString(), OPERATOR_SUBSTRACT)) {
                    // double negative
                    expression.set(i, OPERATOR_ADD);
                    expression.set(i + 1, "");
                } else if (i == 0 && expression.get(i + 1) instanceof Double) {
                    expression.set(i, "");
                    expression.set(i + 1, (double) expression.get(i + 1) * -1);
                } else if (i > 0 && (expression.get(i - 1) instanceof Double) && expression.get(i + 1) instanceof Double) {
                    expression.set(i, "");
                    expression.set(i + 1, (double) expression.get(i + 1) * -1);
                }
            }
        }

        expression.removeIf(n -> n == "");
        return expression;
    }

    private static ArrayList calculate_1_value_expression(ArrayList expression, String operation_symbol, EnumFunctionValueDirection direction, ICalculateFunctionOneValue MyMath) {
        int i = 0;
        while (i < expression.size()) {
            if (Objects.equals(expression.get(i).toString(), operation_symbol)) {
                if (i == 0 && direction == EnumFunctionValueDirection.LEFT) {
                    return ERROR;
                }
                if (i == (expression.size() - 1) && direction == EnumFunctionValueDirection.RIGHT) {
                    return ERROR;
                }
                if (!(expression.get(i - 1) instanceof Double) && direction == EnumFunctionValueDirection.LEFT) {
                    return ERROR;
                }
                if (!(expression.get(i + 1) instanceof Double) && direction == EnumFunctionValueDirection.RIGHT) {
                    return ERROR;
                }

                double result;
                if (direction == EnumFunctionValueDirection.LEFT) {
                    result = MyMath.calculation_function(Double.parseDouble(expression.get(i - 1).toString()));
                    if (result == Double.MIN_VALUE) return ERROR;
                    // Remove unnecessary elements and update value
                    expression.set(i - 1, result);
                    expression.remove(i);
                    i = i - 1;
                } else if (direction == EnumFunctionValueDirection.RIGHT) {
                    result = MyMath.calculation_function(Double.parseDouble(expression.get(i + 1).toString()));
                    if (result == Double.MIN_VALUE) return ERROR;
                    // Remove unnecessary elements and update value
                    expression.set(i, result);
                    expression.remove(i + 1);
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }

        return expression;
    }

    private static ArrayList calculate_2_value_expressions(ArrayList expression, String operation_symbol, ICalculateFunctionTwoValues MyMath) {
        int i = 0;
        while (i < expression.size()) {
            var c = expression.get(i).toString();
            if (Objects.equals(c, operation_symbol)) {
                var prev_num = Double.parseDouble(expression.get(i - 1).toString());
                var next_num = Double.parseDouble(expression.get(i + 1).toString());
                var result = MyMath.calculation_function(prev_num, next_num);
                if (result == Double.MIN_VALUE) return ERROR;
                // Remove unnecessary elements and update value
                expression.set(i - 1, result);
                expression.remove(i);
                expression.remove(i);
                i = i - 1;
            } else {
                i++;
            }
        }
        return expression;
    }

    private static double calculate_factorial(double number) {
        if (number < 0) return 0.0;
        if (number == 0) return 1.0;
        if (number == 1) return 1.0;
        if (number == 2) return 2.0;
        int total = 1;
        for (int n = 1; n <= number; n++) total *= n;
        return total;
    }

    private static double calculate_permutation(double n, double r) {
        if (n < r || n < 0 || r < 0) return Double.MIN_VALUE;
        return calculate_factorial(n) / calculate_factorial(n - r);
    }

    private static double calculate_combinations(double n, double r) {
        if (n < r || n < 0 || r < 0) return Double.MIN_VALUE;
        return calculate_factorial(n) / (calculate_factorial(r) * calculate_factorial(n - r));
    }

    private static double calculate_math(ArrayList expr) {
        try {
            // factorial and nPr and nCr
            ArrayList expression = calculate_1_value_expression(expr, OPERATOR_FACTORIAL, EnumFunctionValueDirection.LEFT, num -> {
                var value = calculate_factorial(num);
                if (value == Double.MIN_VALUE) return Double.MIN_VALUE;
                return value;
            });

            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_2_value_expressions(expression, PERMUTATIONS, Main::calculate_permutation);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_2_value_expressions(expression, COMBINATIONS, Main::calculate_combinations);
            if (expression == ERROR) return Double.MIN_VALUE;

            // calculate trigonometry

            expression = calculate_1_value_expression(expression, OPERATOR_SIN, EnumFunctionValueDirection.RIGHT, Math::sin);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_1_value_expression(expression, OPERATOR_SINH, EnumFunctionValueDirection.RIGHT, Math::sinh);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_1_value_expression(expression, OPERATOR_COS, EnumFunctionValueDirection.RIGHT, Math::cos);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_1_value_expression(expression, OPERATOR_COSH, EnumFunctionValueDirection.RIGHT, Math::cosh);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_1_value_expression(expression, OPERATOR_TAN, EnumFunctionValueDirection.RIGHT, Math::tan);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_1_value_expression(expression, OPERATOR_TANH, EnumFunctionValueDirection.RIGHT, Math::tanh);
            if (expression == ERROR) return Double.MIN_VALUE;

            // calculate logarithms
            expression = calculate_1_value_expression(expression, OPERATOR_LOG10, EnumFunctionValueDirection.RIGHT, (number) -> Math.log(number) / Math.log(10));
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_1_value_expression(expression, OPERATOR_LN, EnumFunctionValueDirection.RIGHT, Math::log);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_2_value_expressions(expression, OPERATOR_LOGx, (a, b) -> Math.log(a) / Math.log(b));
            if (expression == ERROR) return Double.MIN_VALUE;

            // calculate exponents and roots
            expression = calculate_2_value_expressions(expression, OPERATOR_POW, Math::pow);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_2_value_expressions(expression, OPERATOR_ROOT, (a, b) -> Math.pow(b, 1 / a));
            if (expression == ERROR) return Double.MIN_VALUE;

            // calculate basic arithmetic
            expression = calculate_2_value_expressions(expression, OPERATOR_DIVIDE, (a, b) -> a / b);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_2_value_expressions(expression, OPERATOR_MULTPILY, (a, b) -> a * b);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_2_value_expressions(expression, OPERATOR_SUBSTRACT, (a, b) -> a - b);
            if (expression == ERROR) return Double.MIN_VALUE;

            expression = calculate_2_value_expressions(expression, OPERATOR_ADD, Double::sum);
            if (expression == ERROR) return Double.MIN_VALUE;

            if (expression.size() != 1) return Double.MIN_VALUE;
            if (!(expression.get(0) instanceof Double)) return Double.MIN_VALUE;

            return Double.parseDouble(expression.get(0).toString());
        } catch (Exception e) {
            return Double.MIN_VALUE;
        }
    }

    private static ArrayList calculate_innermost_brackets(ArrayList expression, ICalculations MyMath) {
        int last_open_bracket = -1;
        int first_close_bracket = -1;
        int count_open_bracket = 0;
        int count_close_bracket = 0;
        for (int i = 0; i < expression.size(); i++) {
            if (Objects.equals(expression.get(i).toString(), "(")) {
                last_open_bracket = i;
                count_open_bracket += 1;
            } else if (Objects.equals(expression.get(i).toString(), ")")) {
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
                var bracket_expression = (ArrayList) expression.subList(start, end);
                var value = MyMath.calculate_math(bracket_expression);
                if (value == Double.MIN_VALUE) return ERROR;
                expression.set(last_open_bracket, value);

                // remove all elements from last_open_bracket to first_close_bracket
                for (int j = last_open_bracket + 1; j <= first_close_bracket; j++) {
                    expression.remove(last_open_bracket + 1);
                }
                return expression;
            }
        }

        return expression;
    }


    public static void main(String[] args) {

        if (args.length <= 1) {
            System.out.println("PLEASE ADD AN EXPRESSION TO CALCULATE");
            return;
        }

        var expression = new ArrayList();

        // construct expression for arguments e.g 1+1 +2 /4 *4
        // white spaces are automatically handled by joining each argument
        for (int i = 1; i < args.length; i++) {
            for (int j = 0; j < args[i].length(); j++) {
                expression.add(Character.toString(args[i].charAt(j)));
            }
        }

        // create the number from string
        expression = construct_numbers_from_string_of_integers(expression);
        
        // calculate decimal numbers
        expression = construct_decimal_numbers(expression);
        if (expression == ERROR) {
            System.out.println("INVALID DECIMAL NUMBER FORMAT");
            return;
        }

        // replace all PI symbols with value
        for (int i = 0; i < expression.size(); i++) {
            boolean piExists = Arrays.binarySearch(new String[]{"π", "PI", "pi", "p"}, expression.get(i).toString()) >= 0;
            if (piExists) expression.set(i, Math.PI);
        }

        // convert negative numbers
        expression = convert_negative_numbers(expression);
        if (expression == ERROR){
            System.out.println("INVALID NEGATIVE NUMBER FORMAT");
            return;
        }

        // Calculate inner bracket expressions
        do {
            expression = calculate_innermost_brackets(expression, Main::calculate_math);
            if (expression == ERROR) {
                System.out.println("MATH ERROR");
                return;
            }
        } while (expression.contains("("));

        var value = calculate_math(expression);
        if (value == Double.MIN_VALUE)  {
            System.out.println("MATH ERROR");
            return;
        }

        System.out.println(value);
    }
} 