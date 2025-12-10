using System;

class Calculator
{
    static void Main()
    {
        Console.WriteLine("CLI Scientific Calculator (type 'exit' to quit)");

        while (true)
        {
            Console.Write("> ");
            string input = Console.ReadLine();

            if (input.Trim().ToLower() == "exit")
                break;

            try
            {
                var parser = new ExpressionParser(input);
                double result = parser.ParseExpression();
                Console.WriteLine("= " + result);
            }
            catch (Exception ex)
            {
                Console.WriteLine("Error: " + ex.Message);
            }
        }
    }
}

public class ExpressionParser
{
    private string text;
    private int pos;

    public ExpressionParser(string input)
    {
        text = input.Replace(" ", "");
        pos = 0;
    }

    private char Peek() => pos < text.Length ? text[pos] : '\0';
    private char Next() => pos < text.Length ? text[pos++] : '\0';
    private bool Eat(char c)
    {
        if (Peek() == c) { pos++; return true; }
        return false;
    }

    // MAIN: expression → term ((+|-) term)*
    public double ParseExpression()
    {
        double value = ParseTerm();

        while (true)
        {
            if (Eat('+'))
                value += ParseTerm();
            else if (Eat('-'))
                value -= ParseTerm();
            else
                return value;
        }
    }

    // term → factor ((*|/) factor)*
    private double ParseTerm()
    {
        double value = ParseFactor();

        while (true)
        {
            if (Eat('*'))
                value *= ParseFactor();
            else if (Eat('/'))
                value /= ParseFactor();
            else
                return value;
        }
    }

    // factor → unary (^ unary | r unary)*
    private double ParseFactor()
    {
        double value = ParseUnary();

        while (true)
        {
            if (Eat('^'))
                value = Math.Pow(value, ParseUnary());
            else if (Eat('r'))
                value = Math.Pow(ParseUnary(), 1.0 / value);  // a r b = b^(1/a)
            else
                return value;
        }
    }

    // unary → (+|-) unary | primary
    private double ParseUnary()
    {
        if (Eat('+')) return ParseUnary();
        if (Eat('-')) return -ParseUnary();
        return ParsePrimary();
    }

    // primary → number | function primary | '(' expression ')' | factorial
    private double ParsePrimary()
    {
        char c = Peek();

        // Functions: S s C c T h l L r
        if (char.IsLetter(c))
        {
            string f = ReadFunction();
            double arg = ParsePrimary();
            return ApplyFunction(f, arg);
        }

        // Parentheses
        if (Eat('('))
        {
            double val = ParseExpression();
            if (!Eat(')'))
                throw new Exception("Missing closing parenthesis");

            if (Eat('!'))
                return Factorial(val);

            return val;
        }

        // Numbers or π constant
        double number = ParseNumber();

        if (Eat('!'))
            return Factorial(number);

        return number;
    }

    private string ReadFunction()
    {
        string f = "";
        while (char.IsLetter(Peek()))
            f += Next();
        return f;
    }

    private double ParseNumber()
    {
        if (Peek() == 'p') // π constant
        {
            Next();
            return Math.PI;
        }

        string number = "";
        while (char.IsDigit(Peek()) || Peek() == '.')
            number += Next();

        if (number.Length == 0)
            throw new Exception("Number expected");

        return double.Parse(number);
    }

    private double ApplyFunction(string f, double x)
    {
        return f switch
        {
            "S" => Math.Sin(x),
            "s" => Math.Sinh(x),
            "C" => Math.Cos(x),
            "c" => Math.Cosh(x),
            "T" => Math.Tan(x),
            "h" => Math.Tanh(x),
            "l" => Math.Log(x),
            "L" => Math.Log10(x),
            "r" => Math.Sqrt(x),
            _ => throw new Exception("Unknown function: " + f),
        };
    }

    private double Factorial(double n)
    {
        if (n < 0) throw new Exception("Factorial of negative number");
        double result = 1;
        for (int i = 2; i <= (int)n; i++)
            result *= i;
        return result;
    }
}
