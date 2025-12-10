package main;
import java.util.*;

public class CLICalculator {

    // Supported constants
    private static final double p = Math.PI; // 3.14159...

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("CLI Scientific Calculator. Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;

            try {
                double result = evaluate(input);
                System.out.println("= " + result);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Evaluate expression
    public static double evaluate(String expr) {
        expr = expr.replaceAll("\\s+", "");
        expr = replaceFunctions(expr);
        return parseExpression(new Tokenizer(expr));
    }

    // Replace custom syntax with Java-friendly tokens
    private static String replaceFunctions(String expr) {
        return expr
                .replace("S", "sin")
                .replace("s", "sinh")
                .replace("C", "cos")
                .replace("c", "cosh")
                .replace("T", "tan")
                .replace("h", "tanh")
                .replace("l", "ln")
                .replace("L", "log10")
                .replace("p", String.valueOf(p));
    }

    // Tokenizer to read characters
    private static class Tokenizer {
        String s;
        int pos = 0;
        Tokenizer(String s) { this.s = s; }
        char peek() { return pos < s.length() ? s.charAt(pos) : '\0'; }
        char next() { return pos < s.length() ? s.charAt(pos++) : '\0'; }
        boolean eat(char c) { if (peek() == c) { pos++; return true; } return false; }
    }

    // Grammar: expression → term ( ('+' | '-') term )*
    private static double parseExpression(Tokenizer t) {
        double val = parseTerm(t);
        while (true) {
            if (t.eat('+')) val += parseTerm(t);
            else if (t.eat('-')) val -= parseTerm(t);
            else return val;
        }
    }

    // term → factor ( ('*' | '/') factor )*
    private static double parseTerm(Tokenizer t) {
        double val = parseFactor(t);
        while (true) {
            if (t.eat('*')) val *= parseFactor(t);
            else if (t.eat('/')) val /= parseFactor(t);
            else return val;
        }
    }

    // factor → unary ( '^' unary )*
    private static double parseFactor(Tokenizer t) {
        double base = parseUnary(t);
        while (t.eat('^')) {
            base = Math.pow(base, parseUnary(t));
        }
        return base;
    }

    // unary → (+|-) unary | primary
    private static double parseUnary(Tokenizer t) {
        if (t.eat('+')) return parseUnary(t);
        if (t.eat('-')) return -parseUnary(t);
        return parsePrimary(t);
    }

    // primary → number | '(' expression ')' | function primary | factorial
    private static double parsePrimary(Tokenizer t) {
        char c = t.peek();

        // Number
        if ((c >= '0' && c <= '9') || c == '.') {
            StringBuilder sb = new StringBuilder();
            while (Character.isDigit(t.peek()) || t.peek() == '.') sb.append(t.next());
            double val = Double.parseDouble(sb.toString());
            if (t.eat('!')) return factorial(val);
            return val;
        }

        // Parentheses
        if (t.eat('(')) {
            double val = parseExpression(t);
            if (!t.eat(')')) throw new RuntimeException("Missing )");
            if (t.eat('!')) return factorial(val);
            return val;
        }

        // Functions
        String fn = readFunction(t);
        if (!fn.isEmpty()) {
            double arg = parsePrimary(t);
            return applyFunction(fn, arg);
        }

        // Unknown
        throw new RuntimeException("Unexpected: " + t.peek());
    }

    private static String readFunction(Tokenizer t) {
        StringBuilder sb = new StringBuilder();
        while (Character.isAlphabetic(t.peek())) sb.append(t.next());
        return sb.toString();
    }

    private static double applyFunction(String fn, double x) {
        switch (fn) {
            case "sin": return Math.sin(x);
            case "sinh": return Math.sinh(x);
            case "cos": return Math.cos(x);
            case "cosh": return Math.cosh(x);
            case "tan": return Math.tan(x);
            case "tanh": return Math.tanh(x);
            case "ln": return Math.log(x);
            case "log10": return Math.log10(x);
            case "r": return Math.sqrt(x);
            default: throw new RuntimeException("Unknown function: " + fn);
        }
    }

    // Factorial
    private static double factorial(double n) {
        if (n < 0) throw new RuntimeException("Factorial of negative number");
        int k = (int)n;
        double r = 1;
        for (int i = 2; i <= k; i++) r *= i;
        return r;
    }
}
