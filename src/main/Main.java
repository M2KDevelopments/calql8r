import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
    
    public static double factorial(double n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial not defined for negative numbers");
        }
        if (n == 0 || n == 1) {
            return 1;
        }
        
        double result = 1;
        for (int i = 2; i <= (int) n; i++) {
            result *= i;
        }
        return result;
    }
    
    public static double evaluateExpression(String input) {
        String expr = input.replaceAll("\\s+", "");
        
        // Replace p with pi
        expr = expr.replace("p", String.valueOf(Math.PI));
        
        // Handle factorials (n!)
        Pattern factorialPattern = Pattern.compile("(\\d+\\.?\\d*)!");
        Matcher factorialMatcher = factorialPattern.matcher(expr);
        StringBuffer sb = new StringBuffer();
        while (factorialMatcher.find()) {
            double num = Double.parseDouble(factorialMatcher.group(1));
            factorialMatcher.appendReplacement(sb, String.valueOf(factorial(num)));
        }
        factorialMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Handle two-value functions (aFb format)
        
        // Pow (^): a^b
        Pattern powPattern = Pattern.compile("(\\d+\\.?\\d*)\\^(\\d+\\.?\\d*)");
        Matcher powMatcher = powPattern.matcher(expr);
        sb = new StringBuffer();
        while (powMatcher.find()) {
            double a = Double.parseDouble(powMatcher.group(1));
            double b = Double.parseDouble(powMatcher.group(2));
            powMatcher.appendReplacement(sb, String.valueOf(Math.pow(a, b)));
        }
        powMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Root (r): arb means b-th root of a
        Pattern rootPattern = Pattern.compile("(\\d+\\.?\\d*)r(\\d+\\.?\\d*)");
        Matcher rootMatcher = rootPattern.matcher(expr);
        sb = new StringBuffer();
        while (rootMatcher.find()) {
            double a = Double.parseDouble(rootMatcher.group(1));
            double b = Double.parseDouble(rootMatcher.group(2));
            rootMatcher.appendReplacement(sb, String.valueOf(Math.pow(a, 1.0 / b)));
        }
        rootMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Handle one-value functions (Fn format)
        
        // Sin - S
        Pattern sinPattern = Pattern.compile("S(\\d+\\.?\\d*)");
        Matcher sinMatcher = sinPattern.matcher(expr);
        sb = new StringBuffer();
        while (sinMatcher.find()) {
            double n = Double.parseDouble(sinMatcher.group(1));
            sinMatcher.appendReplacement(sb, String.valueOf(Math.sin(n)));
        }
        sinMatcher.appendTail(sb);
        expr = sb.toString();
        
        // SinH - s
        Pattern sinhPattern = Pattern.compile("s(\\d+\\.?\\d*)");
        Matcher sinhMatcher = sinhPattern.matcher(expr);
        sb = new StringBuffer();
        while (sinhMatcher.find()) {
            double n = Double.parseDouble(sinhMatcher.group(1));
            sinhMatcher.appendReplacement(sb, String.valueOf(Math.sinh(n)));
        }
        sinhMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Cos - C
        Pattern cosPattern = Pattern.compile("C(\\d+\\.?\\d*)");
        Matcher cosMatcher = cosPattern.matcher(expr);
        sb = new StringBuffer();
        while (cosMatcher.find()) {
            double n = Double.parseDouble(cosMatcher.group(1));
            cosMatcher.appendReplacement(sb, String.valueOf(Math.cos(n)));
        }
        cosMatcher.appendTail(sb);
        expr = sb.toString();
        
        // CosH - c
        Pattern coshPattern = Pattern.compile("c(\\d+\\.?\\d*)");
        Matcher coshMatcher = coshPattern.matcher(expr);
        sb = new StringBuffer();
        while (coshMatcher.find()) {
            double n = Double.parseDouble(coshMatcher.group(1));
            coshMatcher.appendReplacement(sb, String.valueOf(Math.cosh(n)));
        }
        coshMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Tan - T
        Pattern tanPattern = Pattern.compile("T(\\d+\\.?\\d*)");
        Matcher tanMatcher = tanPattern.matcher(expr);
        sb = new StringBuffer();
        while (tanMatcher.find()) {
            double n = Double.parseDouble(tanMatcher.group(1));
            tanMatcher.appendReplacement(sb, String.valueOf(Math.tan(n)));
        }
        tanMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Tanh - t
        Pattern tanhPattern = Pattern.compile("t(\\d+\\.?\\d*)");
        Matcher tanhMatcher = tanhPattern.matcher(expr);
        sb = new StringBuffer();
        while (tanhMatcher.find()) {
            double n = Double.parseDouble(tanhMatcher.group(1));
            tanhMatcher.appendReplacement(sb, String.valueOf(Math.tanh(n)));
        }
        tanhMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Ln - l
        Pattern lnPattern = Pattern.compile("l(\\d+\\.?\\d*)");
        Matcher lnMatcher = lnPattern.matcher(expr);
        sb = new StringBuffer();
        while (lnMatcher.find()) {
            double n = Double.parseDouble(lnMatcher.group(1));
            lnMatcher.appendReplacement(sb, String.valueOf(Math.log(n)));
        }
        lnMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Log (base 10) - L
        Pattern logPattern = Pattern.compile("L(\\d+\\.?\\d*)");
        Matcher logMatcher = logPattern.matcher(expr);
        sb = new StringBuffer();
        while (logMatcher.find()) {
            double n = Double.parseDouble(logMatcher.group(1));
            logMatcher.appendReplacement(sb, String.valueOf(Math.log10(n)));
        }
        logMatcher.appendTail(sb);
        expr = sb.toString();
        
        // Evaluate the final expression
        return new Parser(expr).parseExpression();
    }
    
    static class Parser {
        private String expr;
        private int pos;
        
        public Parser(String expr) {
            this.expr = expr;
            this.pos = 0;
        }
        
        private Character currentChar() {
            return pos < expr.length() ? expr.charAt(pos) : null;
        }
        
        private void advance() {
            pos++;
        }
        
        private void skipWhitespace() {
            while (currentChar() != null && Character.isWhitespace(currentChar())) {
                advance();
            }
        }
        
        public double parseExpression() {
            double result = parseTerm();
            
            while (true) {
                skipWhitespace();
                Character ch = currentChar();
                
                if (ch != null && ch == '+') {
                    advance();
                    result += parseTerm();
                } else if (ch != null && ch == '-') {
                    advance();
                    result -= parseTerm();
                } else {
                    break;
                }
            }
            
            return result;
        }
        
        private double parseTerm() {
            double result = parseFactor();
            
            while (true) {
                skipWhitespace();
                Character ch = currentChar();
                
                if (ch != null && ch == '*') {
                    advance();
                    result *= parseFactor();
                } else if (ch != null && ch == '/') {
                    advance();
                    result /= parseFactor();
                } else {
                    break;
                }
            }
            
            return result;
        }
        
        private double parseFactor() {
            skipWhitespace();
            
            // Handle negative numbers
            if (currentChar() != null && currentChar() == '-') {
                advance();
                return -parseFactor();
            }
            
            // Handle positive sign
            if (currentChar() != null && currentChar() == '+') {
                advance();
                return parseFactor();
            }
            
            // Handle parentheses
            if (currentChar() != null && currentChar() == '(') {
                advance();
                double result = parseExpression();
                skipWhitespace();
                if (currentChar() != null && currentChar() == ')') {
                    advance();
                } else {
                    throw new IllegalArgumentException("Expected ')'");
                }
                return result;
            }
            
            // Handle numbers
            return parseNumber();
        }
        
        private double parseNumber() {
            skipWhitespace();
            int start = pos;
            
            while (currentChar() != null && (Character.isDigit(currentChar()) || currentChar() == '.')) {
                advance();
            }
            
            if (start == pos) {
                throw new IllegalArgumentException("Expected number at position " + pos);
            }
            
            return Double.parseDouble(expr.substring(start, pos));
        }
    }
    
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("CLI Calculator");
        System.out.println("==================================================");
        System.out.println("\nOperators:");
        System.out.println("  S - Sin      s - SinH");
        System.out.println("  C - Cos      c - CosH");
        System.out.println("  T - Tan      t - TanH");
        System.out.println("  ^ - Power    r - Root (arb = b-th root of a)");
        System.out.println("  l - Ln       L - Log (base 10)");
        System.out.println("  ! - Factorial");
        System.out.println("  p - Pi (3.14159...)");
        System.out.println("\nExamples:");
        System.out.println("  1+2");
        System.out.println("  2+5^4");
        System.out.println("  2*(2+25)");
        System.out.println("  6!");
        System.out.println("  2+4^2");
        System.out.println("  8r3 (cube root of 8)");
        System.out.println("  L100 (log base 10 of 100)");
        System.out.println("  S1.57 (sin of 1.57)");
        System.out.println("\nType 'quit' or 'exit' to exit\n");
        System.out.println("==================================================");
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            try {
                System.out.print("\nEnter expression: ");
                String input = scanner.nextLine().trim();
                
                // Check for exit commands
                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("q")) {
                    System.out.println("Goodbye!");
                    break;
                }
                
                // Skip empty input
                if (input.isEmpty()) {
                    continue;
                }
                
                // Evaluate and display result
                double result = evaluateExpression(input);
                System.out.println("Result: " + result);
                
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
}