using System;
using System.Collections;
using System.Linq; // Required for the Contains method

class CalQl8r
{
    static String DECIMAL_POINT = ".";
    static String OPERATOR_ADD = "+";
    static String OPERATOR_SUBSTRACT = "-";
    static String OPERATOR_MULTPILY = "*";
    static String OPERATOR_DIVIDE = "/";
    static String OPERATOR_LOGx = "l";
    static String OPERATOR_POW = "^";
    static String OPERATOR_ROOT = "r";
    static String OPERATOR_SIN = "S";
    static String OPERATOR_SINH = "s";
    static String OPERATOR_COS = "C";
    static String OPERATOR_COSH = "c";
    static String OPERATOR_TAN = "T";
    static String OPERATOR_TANH = "t";
    static String OPERATOR_LOG10 = "L";
    static String OPERATOR_LN = "E";
    static String OPERATOR_FACTORIAL = "!";

    static String PERMUTATIONS = "Y";
    static String COMBINATIONS = "Z";

    private enum EnumFunctionValueDirection { LEFT, RIGHT }


    private static ArrayList? ConstructNumbersFromStringOfIntegers(ArrayList expression)
    {
        int start = -1;
        int end = 0;
        String[] numbers = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
        for (int i = 0; i < expression!.Count; i++)
        {
            if (numbers.Contains(expression[i]!.ToString()))
            {
                if (start == -1) start = i;
                end = i;
                if ((end == expression!.Count - 1) && start != -1)
                {
                    try
                    {
                        var number = "";
                        for(int j = start; j <= end; j++) {
                            number += expression[j]!.ToString();
                        }
                        var value = int.Parse(number);
                        expression[start] = value;
                        for (int j = start + 1; j <= end; j++) {
                            expression[j] = "";
                        }
                    }
                    catch (Exception)
                    {
                        return null;
                    }
                }
            }
            else
            {
                if (start != -1)
                {
                    try
                    {
                        var number = "";
                        for(int j = start; j <= end; j++) number += expression[j]!.ToString();
                        var value = int.Parse(number);
                        expression[start] = value;
                        for (int j = start + 1; j <= end; j++) expression[j] = "";
                    }
                    catch (Exception)
                    {
                        return null;
                    }
                    start = -1;
                }
            }
        }

        while (expression!.Contains(""))
        {
            expression.Remove("");
        }
        return expression;
    }

    private static ArrayList? ConstructDecimalNumbers(ArrayList expression)
    {
        int i = 0;
        while (i < expression!.Count)
        {
            if ((expression[i]!.ToString() == DECIMAL_POINT))
            {
                if (i == 0 && (i == expression!.Count - 1)) return null;
                try
                {
                    var number = expression[i - 1] + "." + expression[i + 1];
                    expression[i - 1] = double.Parse(number);
                    expression!.RemoveAt(i);
                    expression!.RemoveAt(i);
                }
                catch (Exception)
                {
                    return null;
                }
            }
            else
            {
                i++;
            }
        }
        return expression;
    }

    private static ArrayList? ConvertNegativeNumbers(ArrayList expression)
    {
        for (int i = 0; i < expression!.Count; i++)
        {
            if ((expression[i]!.ToString() == OPERATOR_SUBSTRACT))
            {
                if (i == (expression!.Count - 1)) return null;

                if ((expression[i + 1]!.ToString() == OPERATOR_SUBSTRACT))
                {
                    // double negative
                    expression[i] = OPERATOR_ADD;
                    expression[i + 1] = "";
                }
                else if (i == 0 && expression[i + 1] is double)
                {
                    expression[i] = "";
                    expression[i + 1] = (double)expression[i + 1]! * -1;
                }
                else if (i > 0 && (expression[i - 1] is double) && expression[i + 1] is double)
                {
                    expression[i] = "";
                    expression[i + 1] = (double)expression[i + 1]! * -1;
                }
            }
        }

        while (expression!.Contains(""))
        {
            expression.Remove("");
        }
        return expression;
    }

    private static ArrayList? Calculate1ValueExpression(ArrayList? expression, String operation_symbol, EnumFunctionValueDirection direction, Func<double, double?> CalculationFunction)
    {
        int i = 0;
        while (i < expression!.Count)
        {
            if ((expression[i]!.ToString() == operation_symbol))
            {
                if (i == 0 && direction == EnumFunctionValueDirection.LEFT)
                {
                    return null;
                }
                if (i == (expression!.Count - 1) && direction == EnumFunctionValueDirection.RIGHT)
                {
                    return null;
                }
                if (!(expression[i - 1] is double) && direction == EnumFunctionValueDirection.LEFT)
                {
                    return null;
                }
                if (!(expression[i + 1] is double) && direction == EnumFunctionValueDirection.RIGHT)
                {
                    return null;
                }

                double? result;
                var s = expression[i + 1]?.ToString() ?? "";
                if (direction == EnumFunctionValueDirection.LEFT)
                {
                    double num = double.Parse(s);
                    result = CalculationFunction.Invoke(num);
                    if (result == null) return null;
                    // Remove unnecessary elements and update value
                    expression[i - 1] = result;
                    expression!.RemoveAt(i);
                    i = i - 1;
                }
                else if (direction == EnumFunctionValueDirection.RIGHT)
                {
                    double num = double.Parse(s);
                    result = CalculationFunction.Invoke(num);
                    if (result == null) return null;
                    // Remove unnecessary elements and update value
                    expression[i] = result;
                    expression!.RemoveAt(i + 1);
                }
                else
                {
                    i++;
                }
            }
            else
            {
                i++;
            }
        }

        return expression;
    }

    private static ArrayList? Calculate2ValueExpressions(ArrayList? expression, String operation_symbol, Func<double, double, double?> CalculationFunction)
    {
        int i = 0;
        while (i < expression!.Count)
        {
            var c = expression[i]!.ToString();
            if ((c == operation_symbol))
            {
                var prev_num = double.Parse(expression[i - 1]!.ToString()!);
                var next_num = double.Parse(expression[i + 1]!.ToString()!);
                var result = CalculationFunction.Invoke(prev_num, next_num);
                if (result == null) return null;
                // Remove unnecessary elements and update value
                expression[i - 1] = result;
                expression!.RemoveAt(i);
                expression!.RemoveAt(i);
                i = i - 1;
            }
            else
            {
                i++;
            }
        }
        return expression;
    }

    private static double CalculateFactorial(double number)
    {
        if (number < 0) return 0.0;
        if (number == 0) return 1.0;
        if (number == 1) return 1.0;
        if (number == 2) return 2.0;
        int total = 1;
        for (int n = 1; n <= number; n++) total *= n;
        return total;
    }

    private static double? CalculatePermutation(double n, double r)
    {
        if (n < r || n < 0 || r < 0) return null;
        return CalculateFactorial(n) / CalculateFactorial(n - r);
    }

    private static double? CalculateCombinations(double n, double r)
    {
        if (n < r || n < 0 || r < 0) return null;
        return CalculateFactorial(n) / (CalculateFactorial(r) * CalculateFactorial(n - r));
    }

    private static double? CalculateMath(ArrayList expr)
    {
        try
        {
            // factorial and nPr and nCr
            var expression = Calculate1ValueExpression(expr, CalQl8r.OPERATOR_FACTORIAL, EnumFunctionValueDirection.LEFT, num => CalculateFactorial(num));
            if (expression == null) return null;

            expression = Calculate2ValueExpressions(expression, CalQl8r.PERMUTATIONS, CalculatePermutation);
            if (expression == null) return null;

            expression = Calculate2ValueExpressions(expression, CalQl8r.COMBINATIONS, CalculateCombinations);
            if (expression == null) return null;

            // calculate trigonometry

            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_SIN, EnumFunctionValueDirection.RIGHT, (number) => Math.Sin(number));
            if (expression == null) return null;

            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_SINH, EnumFunctionValueDirection.RIGHT, (number) => Math.Sinh(number));
            if (expression == null) return null;

            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_COS, EnumFunctionValueDirection.RIGHT, (number) => Math.Cos(number));
            if (expression == null) return null;

            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_COSH, EnumFunctionValueDirection.RIGHT, (number) => Math.Cosh(number));
            if (expression == null) return null;

            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_TAN, EnumFunctionValueDirection.RIGHT, (number) => Math.Tan(number));
            if (expression == null) return null;

            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_TANH, EnumFunctionValueDirection.RIGHT, (number) => Math.Tanh(number));
            if (expression == null) return null;

            // calulate logarithms
            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_LOG10, EnumFunctionValueDirection.RIGHT, (number) => Math.Log(number) / Math.Log(10));
            if (expression == null) return null;

            expression = Calculate1ValueExpression(expression, CalQl8r.OPERATOR_LN, EnumFunctionValueDirection.RIGHT, (a) => Math.Log(a));
            if (expression == null) return null;

            expression = Calculate2ValueExpressions(expression, CalQl8r.OPERATOR_LOGx, (a, b) => Math.Log(a) / Math.Log(b));
            if (expression == null) return null;

            // calculate exponents and roots
            expression = Calculate2ValueExpressions(expression, CalQl8r.OPERATOR_POW, (a, b) => Math.Pow(a, b));
            if (expression == null) return null;

            expression = Calculate2ValueExpressions(expression, CalQl8r.OPERATOR_ROOT, (a, b) => Math.Pow(b, 1 / a));
            if (expression == null) return null;

            // calculate basic arithmetic
            expression = Calculate2ValueExpressions(expression, CalQl8r.OPERATOR_DIVIDE, (a, b) => a / b);
            if (expression == null) return null;

            expression = Calculate2ValueExpressions(expression, CalQl8r.OPERATOR_MULTPILY, (a, b) => a * b);
            if (expression == null) return null;

            expression = Calculate2ValueExpressions(expression, CalQl8r.OPERATOR_SUBSTRACT, (a, b) => a - b);
            if (expression == null) return null;

            expression = Calculate2ValueExpressions(expression, CalQl8r.OPERATOR_ADD, (a, b) => a + b);
            if (expression == null) return null;

            if (expression!.Count != 1) return null;
            if (!(expression[0] is double)) return null;

            return double.Parse(expression[0]!.ToString()!);
        }
        catch (Exception)
        {
            return null;
        }
    }

    private static ArrayList? CalculateInnermostBrackets(ArrayList expression, Func<ArrayList, double?> Calculation)
    {
        int last_open_bracket = -1;
        int first_close_bracket = -1;
        int count_open_bracket = 0;
        int count_close_bracket = 0;
        for (int i = 0; i < expression!.Count; i++)
        {
            if ((expression[i]!.ToString() == "("))
            {
                last_open_bracket = i;
                count_open_bracket += 1;
            }
            else if ((expression[i]!.ToString() == ")"))
            {
                count_close_bracket += 1;
                if (first_close_bracket == -1)
                {
                    first_close_bracket = i;
                }
            }

            // Syntax error
            if (count_close_bracket > count_open_bracket) return null;

            // when the number of open brackets and closing brackets match.
            // 'last_open_bracket' is the start and 'first_close_bracket' is the end. for the calculation
            if (count_open_bracket == count_close_bracket && (first_close_bracket != -1))
            {
                var start = last_open_bracket + 1;
                var end = first_close_bracket;
                var bracket_expression = new ArrayList();
                for(int j = start; j < end; j++) {
                    bracket_expression.Add(expression[j]);
                }
 
                var value = Calculation(bracket_expression);
                if (value == null) return null;
 
                expression[last_open_bracket] = value;
    
                // remove all elements from last_open_bracket to first_close_bracket
                for (int j = last_open_bracket + 1; j <= first_close_bracket; j++)
                {
                    expression!.RemoveAt(last_open_bracket + 1);
                }
                return expression;
            }
        }

        return expression;
    }




    static void Main(string[] args)
    {

        var expression = new ArrayList();

        // construct expression for arguments e.g 1+1 +2 /4 *4
        // white spaces are automatically handled by joining each argument
        for (int i = 0; i < args.Length; i++) {
            for (int j = 0; j < args[i].Length; j++) {
                expression.Add(args[i][j].ToString());
            }
        }

        // create the number from string
        expression = ConstructNumbersFromStringOfIntegers(expression);
        if (expression == null) {
            Console.WriteLine("INVALID NUMBER FORMAT");
            return;
        }

        // calculate decimal numbers
        expression = ConstructDecimalNumbers(expression!);
        if (expression == null) {
            Console.WriteLine("INVALID DECIMAL NUMBER FORMAT");
            return;
        }

        // replace all PI symbols with value
        string[] pis = {"π", "PI", "pi", "p"};
        for (int i = 0; i < expression!.Count; i++) {
            bool piExists = pis.Contains(expression[i]!.ToString());
            if (piExists) expression[i] = Math.PI;
        }

        // convert negative numbers
        expression = ConvertNegativeNumbers(expression!);
        if (expression == null){
            Console.WriteLine("INVALID NEGATIVE NUMBER FORMAT");
            return;
        }

        // Calculate inner bracket expressions
        do {
            expression = CalculateInnermostBrackets(expression, (list) => CalculateMath(list));
            if (expression == null) {
                Console.WriteLine("MATH ERROR");
                return;
            }
        } while (expression!.Contains("("));

        var value = CalculateMath(expression);
        if (value == null)  {
            Console.WriteLine("MATH ERROR");
            return;
        }

        Console.WriteLine(value);
    }
}