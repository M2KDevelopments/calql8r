using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using System.Globalization;

public class CliCalculator
{
    // --- 1. Operator and Function Definitions ---

    // Define custom function types for clarity
    public delegate double BinaryFunction(double a, double b);
    public delegate double UnaryFunction(double a);
    
    // Store all known operators and functions in a static dictionary for lookup
    private static readonly Dictionary<string, OpFuncDef> Definitions = new Dictionary<string, OpFuncDef>();

    private class OpFuncDef
    {
        public string Name { get; }
        public int Precedence { get; }
        public bool IsLeftAssociative { get; }
        public int Arity { get; } // 1 for unary, 2 for binary
        public Delegate Function { get; }

        public OpFuncDef(string name, int precedence, bool isLeft, int arity, Delegate function)
        {
            Name = name;
            Precedence = precedence;
            IsLeftAssociative = isLeft;
            Arity = arity;
            Function = function;
        }
    }

    // --- 2. Function Implementations ---
    
    // Binary Functions
    private static double Add(double a, double b) => a + b;
    private static double Subtract(double a, double b) => a - b;
    private static double Multiply(double a, double b) => a * b;
    private static double Divide(double a, double b) => a / b;
    private static double Power(double a, double b) => Math.Pow(a, b);
    
    // Custom Root: a r b means b-th root of a. (Root degree is b)
    private static double Root(double a, double b)
    {
        if (a < 0 && Math.Abs(b % 2.0) < double.Epsilon)
            throw new ArgumentException("Cannot take even root of a negative number.");
        return Math.Pow(a, 1.0 / b);
    }
    
    // Unary Functions
    private static double Sin(double a) => Math.Sin(a);
    private static double SinH(double a) => Math.Sinh(a);
    private static double Cos(double a) => Math.Cos(a);
    private static double CosH(double a) => Math.Cosh(a);
    private static double Tan(double a) => Math.Tan(a);
    private static double TanH(double a) => Math.Tanh(a);
    private static double Ln(double a) => Math.Log(a);
    private static double Log10(double a) => Math.Log10(a);

    // Unary Post-fix Factorial
    private static double Factorial(double a)
    {
        if (a < 0 || Math.Abs(a % 1.0) > double.Epsilon)
            throw new ArgumentException("Factorial only defined for non-negative integers.");
        
        long res = 1;
        for (int i = 1; i <= (int)a; i++)
        {
            res *= i;
        }
        return (double)res;
    }

    // --- 3. Static Constructor for Definitions (similar to a lookup table) ---

    static CliCalculator()
    {
        // Precedence: Higher number binds tighter
        
        // Postfix Unary (Highest)
        Definitions.Add("!", new OpFuncDef("!", 6, true, 1, (UnaryFunction)Factorial)); 

        // Prefix Unary Functions
        // Note: Right Associative precedence 5
        Definitions.Add("S", new OpFuncDef("S", 5, false, 1, (UnaryFunction)Sin));
        Definitions.Add("s", new OpFuncDef("s", 5, false, 1, (UnaryFunction)SinH));
        Definitions.Add("C", new OpFuncDef("C", 5, false, 1, (UnaryFunction)Cos));
        Definitions.Add("c", new OpFuncDef("c", 5, false, 1, (UnaryFunction)CosH));
        Definitions.Add("T", new OpFuncDef("T", 5, false, 1, (UnaryFunction)Tan));
        Definitions.Add("t", new OpFuncDef("t", 5, false, 1, (UnaryFunction)TanH));
        Definitions.Add("l", new OpFuncDef("l", 5, false, 1, (UnaryFunction)Ln));    // Ln
        Definitions.Add("L", new OpFuncDef("L", 5, false, 1, (UnaryFunction)Log10)); // Log10

        // Binary Operators
        Definitions.Add("^", new OpFuncDef("^", 4, false, 2, (BinaryFunction)Power)); // Right Associative
        Definitions.Add("r", new OpFuncDef("r", 4, true, 2, (BinaryFunction)Root));   // Left Associative

        Definitions.Add("*", new OpFuncDef("*", 3, true, 2, (BinaryFunction)Multiply));
        Definitions.Add("/", new OpFuncDef("/", 3, true, 2, (BinaryFunction)Divide));
        
        Definitions.Add("+", new OpFuncDef("+", 2, true, 2, (BinaryFunction)Add));
        Definitions.Add("-", new OpFuncDef("-", 2, true, 2, (BinaryFunction)Subtract)); // Binary subtraction
    }

    // --- 4. Lexer/Tokenizer ---
    
    // Regular expression to break the string into tokens.
    // This is complex to handle all cases (numbers, ops, funcs, parentheses).
    private static readonly Regex TokenizerRegex = new Regex(
        // Group 1: Numbers (integers or decimals)
        @"(\d+(\.\d*)?|\.\d+)" +
        // Group 2: Functions (S, s, C, c, T, t, l, L)
        @"|([SsCcTtLl])" + 
        // Group 3: Operators (^, *, /, r, +, -, !) 
        // Note: The minus sign '-' is handled as an operator here.
        @"|(\^|\*|/|r|\+|\-|!)" +
        // Group 4: Parentheses and constant 'p'
        @"|([()])" +
        @"|(p)",
        RegexOptions.Compiled | RegexOptions.IgnorePatternWhitespace);
    
    private static List<string> Tokenize(string expression)
    {
        // 1. Preprocess the expression to insert spaces around operators, 
        //    but *not* break up numbers or function names.
        // This is necessary because C#'s Regex MatchCollection doesn't handle overlapping matches well.
        expression = expression.Replace(" ", "").Replace("p", " p ").Replace("(", " ( ").Replace(")", " ) ");

        var tokens = new List<string>();
        var matches = TokenizerRegex.Matches(expression);
        
        foreach (Match match in matches)
        {
            // Only capture the non-empty group value
            tokens.Add(match.Value);
        }

        // The custom tokenizer is tricky in C# for this level of complexity.
        // A simple approach: split by all recognized symbols while preserving the symbols.
        var splitTokens = new List<string>();
        string[] separators = { "(", ")", "+", "-", "*", "/", "^", "!", "r", "S", "s", "C", "c", "T", "t", "l", "L", "p" };

        var current = expression;
        
        foreach (var sep in separators)
        {
            // Use a temporary list to rebuild the expression tokens after splitting
            var tempTokens = new List<string>();
            foreach (var part in current.Split(new[] { sep }, StringSplitOptions.RemoveEmptyEntries))
            {
                if (part.Length > 0)
                {
                    tempTokens.Add(part);
                    // Add the separator back in if it's not the last part
                    if (!part.Equals(current.Split(new[] { sep }, StringSplitOptions.RemoveEmptyEntries).Last()) || current.EndsWith(sep))
                    {
                        // To avoid infinite loop for unary minus, we check if the separator 
                        // should be added back based on the context.
                    }
                }
            }
            // A truly robust tokenizer requires state, but we can simplify by using a 
            // cleaner replacement strategy to separate all tokens with spaces.
        }
        
        // --- The robust tokenization is simplified for the single file requirement ---
        // A manual replacement approach is more reliable here than a single Regex:
        string spacedExpression = expression;
        foreach (var op in separators.OrderByDescending(s => s.Length)) // Longer tokens first
        {
            // Avoid separating a minus sign used for a negative number if possible
            if (op.Length == 1 && char.IsLetterOrDigit(op[0])) continue; // Skip functions/p for now
            
            spacedExpression = spacedExpression.Replace(op, $" {op} ");
        }

        // Handle functions and constant 'p'
        foreach(var key in Definitions.Keys.Where(k => k.Length == 1))
        {
            // Must avoid double spacing
            spacedExpression = spacedExpression.Replace($" {key} ", $" {key} ");
            spacedExpression = spacedExpression.Replace(key, $" {key} ");
        }
        spacedExpression = spacedExpression.Replace("p", " p ");
        
        // Clean up multiple spaces and trim
        tokens = spacedExpression
            .Split(new[] { ' ' }, StringSplitOptions.RemoveEmptyEntries)
            .ToList();
        
        // Handle Unary Minus: If '-' follows '(', an operator, or is the first token, it's unary.
        for (int i = 0; i < tokens.Count; i++)
        {
            if (tokens[i] == "-")
            {
                bool isUnary = i == 0 || tokens[i - 1] == "(" || Definitions.ContainsKey(tokens[i - 1]);

                if (isUnary)
                {
                    // Merge the unary minus with the following number or function (e.g., "-5" or "-S")
                    if (i + 1 < tokens.Count)
                    {
                        tokens[i] = "-" + tokens[i + 1];
                        tokens.RemoveAt(i + 1);
                    }
                }
            }
        }
        
        return tokens;
    }


    // --- 5. Shunting-Yard Algorithm (Infix to RPN) ---
    
    public static List<string> InfixToRpn(List<string> infixTokens)
    {
        var outputQueue = new List<string>();
        var operatorStack = new Stack<string>();
        
        foreach (var token in infixTokens)
        {
            // 1. If the token is a number or constant 'p', add it to the output queue.
            if (double.TryParse(token, NumberStyles.Any, CultureInfo.InvariantCulture, out _) || token == "p")
            {
                outputQueue.Add(token);
            }
            // 2. If the token is a function
            else if (Definitions.TryGetValue(token, out var def) && def.Arity == 1 && token != "!")
            {
                operatorStack.Push(token);
            }
            // 3. If the token is an operator
            else if (Definitions.TryGetValue(token, out def) && def.Arity == 2)
            {
                while (operatorStack.Count > 0)
                {
                    var topToken = operatorStack.Peek();
                    if (Definitions.TryGetValue(topToken, out var topDef))
                    {
                        // Check precedence and associativity
                        if ((def.IsLeftAssociative && def.Precedence <= topDef.Precedence) ||
                            (!def.IsLeftAssociative && def.Precedence < topDef.Precedence))
                        {
                            outputQueue.Add(operatorStack.Pop());
                        }
                        else
                        {
                            break;
                        }
                    }
                    else if (topToken == "(") // Stop if we hit a parenthesis
                    {
                        break;
                    }
                    else // Should only be a function on top (precedence check handles this)
                    {
                        break;
                    }
                }
                operatorStack.Push(token);
            }
            // 4. If the token is a postfix operator '!'
            else if (token == "!")
            {
                 outputQueue.Add(token); // Postfix operator immediately goes to output
            }
            // 5. If the token is '(', push it onto the operator stack.
            else if (token == "(")
            {
                operatorStack.Push(token);
            }
            // 6. If the token is ')', pop operators to the output queue until '(' is found.
            else if (token == ")")
            {
                while (operatorStack.Count > 0 && operatorStack.Peek() != "(")
                {
                    outputQueue.Add(operatorStack.Pop());
                }
                if (operatorStack.Count == 0)
                    throw new ArgumentException("Mismatched parentheses in expression.");
                
                operatorStack.Pop(); // Pop the '('
                
                // If there is a function token at the top of the stack, pop it to the output queue.
                if (operatorStack.Count > 0 && Definitions.ContainsKey(operatorStack.Peek()))
                {
                    outputQueue.Add(operatorStack.Pop());
                }
            }
            else
            {
                throw new ArgumentException($"Invalid token found: {token}");
            }
        }
        
        // 7. Pop remaining operators from the stack to the output queue.
        while (operatorStack.Count > 0)
        {
            var token = operatorStack.Pop();
            if (token == "(")
                throw new ArgumentException("Mismatched parentheses in expression.");
            
            outputQueue.Add(token);
        }
        
        return outputQueue;
    }


    // --- 6. RPN Evaluation ---
    
    public static double EvaluateRpn(List<string> rpnTokens)
    {
        var valueStack = new Stack<double>();

        foreach (var token in rpnTokens)
        {
            // 1. If the token is a number or 'p', push the value onto the stack.
            if (double.TryParse(token, NumberStyles.Any, CultureInfo.InvariantCulture, out double number))
            {
                valueStack.Push(number);
            }
            else if (token == "p")
            {
                valueStack.Push(Math.PI);
            }
            // 2. If the token is a function or operator.
            else if (Definitions.TryGetValue(token, out var def))
            {
                if (def.Arity == 2) // Binary
                {
                    if (valueStack.Count < 2) throw new ArgumentException($"Binary operator '{token}' requires two operands.");
                    double b = valueStack.Pop();
                    double a = valueStack.Pop();
                    valueStack.Push(((BinaryFunction)def.Function)(a, b));
                }
                else if (def.Arity == 1) // Unary (Prefix or Postfix)
                {
                    if (valueStack.Count < 1) throw new ArgumentException($"Unary operator '{token}' requires one operand.");
                    double a = valueStack.Pop();
                    valueStack.Push(((UnaryFunction)def.Function)(a));
                }
            }
            else
            {
                throw new ArgumentException($"Unknown token during evaluation: {token}");
            }
        }

        if (valueStack.Count != 1)
            throw new ArgumentException("Invalid RPN expression (too many/few operands).");

        return valueStack.Pop();
    }

    // --- 7. Main Execution Flow ---
    
    public static double Calculate(string expression)
    {
        var tokens = Tokenize(expression);
        var rpn = InfixToRpn(tokens);
        return EvaluateRpn(rpn);
    }
    
    // --- 8. Main CLI Loop ---
    
    public static void Main(string[] args)
    {
        Console.WriteLine("--- C# CLI Calculator (Infix Mode) ---");
        Console.WriteLine("Supported Operations:");
        Console.WriteLine("  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)");
        Console.WriteLine("  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S 30)");
        Console.WriteLine("  Unary (Postfix): ! (Factorial, e.g., 6!)");
        Console.WriteLine("  Constant: p (PI)");
        Console.WriteLine("\nNOTE: Use explicit multiplication where necessary (e.g., 2*(3) -> 2*(3)).");
        Console.WriteLine("Type 'exit' or 'quit' to end.\n");

        while (true)
        {
            Console.Write("Expression: ");
            string input = Console.ReadLine()?.Trim();

            if (string.IsNullOrEmpty(input) || input.Equals("exit", StringComparison.OrdinalIgnoreCase) || input.Equals("quit", StringComparison.OrdinalIgnoreCase))
            {
                break;
            }

            try
            {
                double result = Calculate(input);
                Console.WriteLine($"Result: **{result:F10}**\n");
            }
            catch (ArgumentException ex)
            {
                Console.WriteLine($"Error: Invalid expression. {ex.Message}\n");
            }
            catch (DivideByZeroException)
            {
                Console.WriteLine("Error: Division by zero.\n");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"An unexpected error occurred: {ex.Message}\n");
            }
        }

        Console.WriteLine("Exiting calculator. Goodbye!");
    }
}