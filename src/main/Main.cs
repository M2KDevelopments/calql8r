using System;
using System.Text.RegularExpressions;

class Calculator
{
    static double Factorial(double n)
    {
        if (n < 0)
        {
            throw new ArgumentException("Factorial not defined for negative numbers");
        }
        if (n == 0 || n == 1)
        {
            return 1;
        }
        
        double result = 1;
        for (int i = 2; i <= (int)n; i++)
        {
            result *= i;
        }
        return result;
    }
    
    static double EvaluateExpression(string input)
    {
        string expr = Regex.Replace(input, @"\s+", "");
        
        // Replace p with pi
        expr = expr.Replace("p", Math.PI.ToString());
        
        // Handle factorials (n!)
        expr = Regex.Replace(expr, @"(\d+\.?\d*)!", match =>
        {
            double num = double.Parse(match.Groups[1].Value);
            return Factorial(num).ToString();
        });
        
        // Handle two-value functions (aFb format)
        
        // Pow (^): a^b
        expr = Regex.Replace(expr, @"(\d+\.?\d*)\^(\d+\.?\d*)", match =>
        {
            double a = double.Parse(match.Groups[1].Value);
            double b = double.Parse(match.Groups[2].Value);
            return Math.Pow(a, b).ToString();
        });
        
        // Root (r): arb means b-th root of a
        expr = Regex.Replace(expr, @"(\d+\.?\d*)r(\d+\.?\d*)", match =>
        {
            double a = double.Parse(match.Groups[1].Value);
            double b = double.Parse(match.Groups[2].Value);
            return Math.Pow(a, 1.0 / b).ToString();
        });
        
        // Handle one-value functions (Fn format)
        
        // Sin - S
        expr = Regex.Replace(expr, @"S(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Sin(n).ToString();
        });
        
        // SinH - s
        expr = Regex.Replace(expr, @"s(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Sinh(n).ToString();
        });
        
        // Cos - C
        expr = Regex.Replace(expr, @"C(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Cos(n).ToString();
        });
        
        // CosH - c
        expr = Regex.Replace(expr, @"c(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Cosh(n).ToString();
        });
        
        // Tan - T
        expr = Regex.Replace(expr, @"T(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Tan(n).ToString();
        });
        
        // Tanh - t
        expr = Regex.Replace(expr, @"t(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Tanh(n).ToString();
        });
        
        // Ln - l
        expr = Regex.Replace(expr, @"l(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Log(n).ToString();
        });
        
        // Log (base 10) - L
        expr = Regex.Replace(expr, @"L(\d+\.?\d*)", match =>
        {
            double n = double.Parse(match.Groups[1].Value);
            return Math.Log10(n).ToString();
        });
        
        // Evaluate the final expression
        return new Parser(expr).ParseExpression();
    }
    
    class Parser
    {
        private string expr;
        private int pos;
        
        public Parser(string expr)
        {
            this.expr = expr;
            this.pos = 0;
        }
        
        private char? CurrentChar()
        {
            return pos < expr.Length ? expr[pos] : (char?)null;
        }
        
        private void Advance()
        {
            pos++;
        }
        
        private void SkipWhitespace()
        {
            while (CurrentChar() != null && char.IsWhiteSpace(CurrentChar().Value))
            {
                Advance();
            }
        }
        
        public double ParseExpression()
        {
            double result = ParseTerm();
            
            while (true)
            {
                SkipWhitespace();
                char? ch = CurrentChar();
                
                if (ch == '+')
                {
                    Advance();
                    result += ParseTerm();
                }
                else if (ch == '-')
                {
                    Advance();
                    result -= ParseTerm();
                }
                else
                {
                    break;
                }
            }
            
            return result;
        }
        
        private double ParseTerm()
        {
            double result = ParseFactor();
            
            while (true)
            {
                SkipWhitespace();
                char? ch = CurrentChar();
                
                if (ch == '*')
                {
                    Advance();
                    result *= ParseFactor();
                }
                else if (ch == '/')
                {
                    Advance();
                    result /= ParseFactor();
                }
                else
                {
                    break;
                }
            }
            
            return result;
        }
        
        private double ParseFactor()
        {
            SkipWhitespace();
            
            // Handle negative numbers
            if (CurrentChar() == '-')
            {
                Advance();
                return -ParseFactor();
            }
            
            // Handle positive sign
            if (CurrentChar() == '+')
            {
                Advance();
                return ParseFactor();
            }
            
            // Handle parentheses
            if (CurrentChar() == '(')
            {
                Advance();
                double result = ParseExpression();
                SkipWhitespace();
                if (CurrentChar() == ')')
                {
                    Advance();
                }
                else
                {
                    throw new FormatException("Expected ')'");
                }
                return result;
            }
            
            // Handle numbers
            return ParseNumber();
        }
        
        private double ParseNumber()
        {
            SkipWhitespace();
            int start = pos;
            
            while (CurrentChar() != null && (char.IsDigit(CurrentChar().Value) || CurrentChar() == '.'))
            {
                Advance();
            }
            
            if (start == pos)
            {
                throw new FormatException($"Expected number at position {pos}");
            }
            
            return double.Parse(expr.Substring(start, pos - start));
        }
    }
    
    static void Main(string[] args)
    {
        Console.WriteLine(new string('=', 50));
        Console.WriteLine("CLI Calculator");
        Console.WriteLine(new string('=', 50));
        Console.WriteLine("\nOperators:");
        Console.WriteLine("  S - Sin      s - SinH");
        Console.WriteLine("  C - Cos      c - CosH");
        Console.WriteLine("  T - Tan      t - TanH");
        Console.WriteLine("  ^ - Power    r - Root (arb = b-th root of a)");
        Console.WriteLine("  l - Ln       L - Log (base 10)");
        Console.WriteLine("  ! - Factorial");
        Console.WriteLine("  p - Pi (3.14159...)");
        Console.WriteLine("\nExamples:");
        Console.WriteLine("  1+2");
        Console.WriteLine("  2+5^4");
        Console.WriteLine("  2*(2+25)");
        Console.WriteLine("  6!");
        Console.WriteLine("  2+4^2");
        Console.WriteLine("  8r3 (cube root of 8)");
        Console.WriteLine("  L100 (log base 10 of 100)");
        Console.WriteLine("  S1.57 (sin of 1.57)");
        Console.WriteLine("\nType 'quit' or 'exit' to exit\n");
        Console.WriteLine(new string('=', 50));
        
        while (true)
        {
            try
            {
                Console.Write("\nEnter expression: ");
                string input = Console.ReadLine()?.Trim() ?? "";
                
                // Check for exit commands
                if (input.Equals("quit", StringComparison.OrdinalIgnoreCase) || 
                    input.Equals("exit", StringComparison.OrdinalIgnoreCase) || 
                    input.Equals("q", StringComparison.OrdinalIgnoreCase))
                {
                    Console.WriteLine("Goodbye!");
                    break;
                }
                
                // Skip empty input
                if (string.IsNullOrEmpty(input))
                {
                    continue;
                }
                
                // Evaluate and display result
                double result = EvaluateExpression(input);
                Console.WriteLine($"Result: {result}");
            }
            catch (ArgumentException ex)
            {
                Console.WriteLine($"Error: {ex.Message}");
            }
            catch (FormatException ex)
            {
                Console.WriteLine($"Error: {ex.Message}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Unexpected error: {ex.Message}");
            }
        }
    }
}