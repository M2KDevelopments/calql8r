import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CLI Calculator program written in a single file using the Shunting-Yard Algorithm 
 * for infix-to-RPN conversion and stack-based evaluation.
 */
public class CliCalculator {

    // --- 1. Operator and Function Definitions ---

    // Define custom functional interfaces for clarity
    @FunctionalInterface
    interface BinaryFunction {
        double apply(double a, double b);
    }

    @FunctionalInterface
    interface UnaryFunction {
        double apply(double a);
    }

    private static class OpFuncDef {
        final String name;
        final int precedence;
        final boolean isLeftAssociative;
        final int arity; // 1 for unary, 2 for binary
        final Object function; // Can be BinaryFunction or UnaryFunction

        OpFuncDef(String name, int precedence, boolean isLeft, int arity, Object function) {
            this.name = name;
            this.precedence = precedence;
            this.isLeftAssociative = isLeft;
            this.arity = arity;
            this.function = function;
        }
    }

    // Global static map for all known operators and functions
    private static final Map<String, OpFuncDef> DEFINITIONS = new HashMap<>();

    // --- 2. Function Implementations ---

    // Binary Functions
    private static double power(double a, double b) { return Math.pow(a, b); }
    
    // Custom Root: a r b means b-th root of a. (Root degree is b)
    private static double root(double a, double b) {
        if (a < 0 && Math.abs(b % 2.0) < 1e-9) // Check if b is an even integer
            throw new IllegalArgumentException("Cannot take even root of a negative number.");
        return Math.pow(a, 1.0 / b);
    }
    
    // Unary Post-fix Factorial
    private static double factorial(double a) {
        if (a < 0 || Math.abs(a % 1.0) > 1e-9)
            throw new IllegalArgumentException("Factorial only defined for non-negative integers.");
        
        long res = 1;
        for (int i = 1; i <= (int)a; i++) {
            res *= i;
        }
        return (double)res;
    }

    // --- 3. Static Initialization Block ---

    static {
        // Precedence: Higher number binds tighter
        
        // Postfix Unary (Highest Precedence)
        DEFINITIONS.put("!", new OpFuncDef("!", 6, true, 1, (UnaryFunction)CliCalculator::factorial)); 

        // Prefix Unary Functions (Right Associative precedence 5)
        DEFINITIONS.put("S", new OpFuncDef("S", 5, false, 1, (UnaryFunction)Math::sin));
        DEFINITIONS.put("s", new OpFuncDef("s", 5, false, 1, (UnaryFunction)Math::sinh));
        DEFINITIONS.put("C", new OpFuncDef("C", 5, false, 1, (UnaryFunction)Math::cos));
        DEFINITIONS.put("c", new OpFuncDef("c", 5, false, 1, (UnaryFunction)Math::cosh));
        DEFINITIONS.put("T", new OpFuncDef("T", 5, false, 1, (UnaryFunction)Math::tan));
        DEFINITIONS.put("t", new OpFuncDef("t", 5, false, 1, (UnaryFunction)Math::tanh));
        DEFINITIONS.put("l", new OpFuncDef("l", 5, false, 1, (UnaryFunction)Math::log));    // Ln
        DEFINITIONS.put("L", new OpFuncDef("L", 5, false, 1, (UnaryFunction)Math::log10)); // Log10

        // Binary Operators
        DEFINITIONS.put("^", new OpFuncDef("^", 4, false, 2, (BinaryFunction)CliCalculator::power));
        DEFINITIONS.put("r", new OpFuncDef("r", 4, true, 2, (BinaryFunction)CliCalculator::root));

        DEFINITIONS.put("*", new OpFuncDef("*", 3, true, 2, (BinaryFunction)(a, b) -> a * b));
        DEFINITIONS.put("/", new OpFuncDef("/", 3, true, 2, (BinaryFunction)(a, b) -> a / b));
        
        DEFINITIONS.put("+", new OpFuncDef("+", 2, true, 2, (BinaryFunction)(a, b) -> a + b));
        // We use an internal token, '_', for binary subtraction to distinguish it from unary minus.
        DEFINITIONS.put("_", new OpFuncDef("-", 2, true, 2, (BinaryFunction)(a, b) -> a - b)); 
    }

    // --- 4. Lexer/Tokenizer ---
    
    /**
     * Converts the raw input string into a list of tokens, handling unary minus 
     * and ensuring tokens are separated for parsing.
     */
    private static List<String> tokenize(String expression) {
        // 1. Preprocess: Insert spaces around all single-character tokens (except inside numbers)
        String processed = expression.replaceAll("\\s+", ""); // Remove all existing spaces

        String[] separators = {"(", ")", "+", "*", "/", "^", "!", "r", "S", "s", "C", "c", "T", "t", "l", "L", "p"};
        for (String sep : separators) {
            // Use Lookahead/Lookbehind to avoid separating parts of numbers (though not strictly necessary here)
            processed = processed.replaceAll(Pattern.quote(sep), " " + sep + " ");
        }
        
        // Separate minus sign, which will be the focus of the next step
        processed = processed.replaceAll("-", " - ");
        
        // Clean up multiple spaces and split
        List<String> tokens = new ArrayList<>(Arrays.asList(processed.split("\\s+")));
        tokens.removeIf(String::isEmpty);

        // 2. Handle Unary Minus vs. Binary Minus
        List<String> resultTokens = new ArrayList<>();
        
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            
            if (token.equals("-")) {
                // Check if the preceding token is an operand (number, constant, '!', or ')')
                boolean isPrecedingTokenOperand = !resultTokens.isEmpty() && 
                    (isNumberOrConstant(resultTokens.get(resultTokens.size() - 1)) || resultTokens.get(resultTokens.size() - 1).equals("!") || resultTokens.get(resultTokens.size() - 1).equals(")") || resultTokens.get(resultTokens.size() - 1).equals("p"));
                
                if (i == 0 || !isPrecedingTokenOperand) {
                    // Unary Minus: Merge it with the next token (e.g., "-5")
                    if (i + 1 < tokens.size()) {
                        tokens.set(i + 1, token + tokens.get(i + 1));
                    }
                    // Skip the current token as it's merged into the next
                } else {
                    // Binary Minus: Use the internal token '_'
                    resultTokens.add("_");
                }
            } else {
                resultTokens.add(token);
            }
        }
        return resultTokens;
    }
    
    private static boolean isNumberOrConstant(String s) {
        if (s.equals("p")) return true;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // --- 5. Shunting-Yard Algorithm (Infix to RPN) ---
    
    /**
     * Converts a list of infix tokens to a list of RPN tokens using Shunting-Yard.
     */
    private static List<String> infixToRpn(List<String> infixTokens) {
        List<String> outputQueue = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();
        
        for (String token : infixTokens) {
            // 1. If the token is a number or constant 'p', add it to the output queue.
            if (isNumberOrConstant(token)) {
                outputQueue.add(token);
            }
            // 2. If the token is a function or prefix unary operator (not '!')
            else if (DEFINITIONS.containsKey(token) && DEFINITIONS.get(token).arity == 1 && !token.equals("!")) {
                operatorStack.push(token);
            }
            // 3. If the token is a binary operator ('_' is binary subtraction)
            else if (DEFINITIONS.containsKey(token) && DEFINITIONS.get(token).arity == 2) {
                OpFuncDef currentDef = DEFINITIONS.get(token);
                
                while (!operatorStack.isEmpty()) {
                    String topToken = operatorStack.peek();
                    if (topToken.equals("(")) break;

                    OpFuncDef topDef = DEFINITIONS.get(topToken);
                    if (topDef == null) break; 
                    
                    // Check precedence and associativity
                    boolean isHigherPrecedence = currentDef.precedence < topDef.precedence;
                    boolean isSamePrecedenceLeftAssoc = currentDef.precedence == topDef.precedence && currentDef.isLeftAssociative;

                    if (isHigherPrecedence || isSamePrecedenceLeftAssoc) {
                        outputQueue.add(operatorStack.pop());
                    } else {
                        break;
                    }
                }
                operatorStack.push(token);
            }
            // 4. If the token is a postfix operator '!'
            else if (token.equals("!")) {
                 outputQueue.add(token); // Postfix operator immediately goes to output
            }
            // 5. If the token is '(', push it onto the operator stack.
            else if (token.equals("(")) {
                operatorStack.push(token);
            }
            // 6. If the token is ')', pop operators to the output queue until '(' is found.
            else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    outputQueue.add(operatorStack.pop());
                }
                if (operatorStack.isEmpty())
                    throw new IllegalArgumentException("Mismatched parentheses in expression: missing '('");
                
                operatorStack.pop(); // Pop the '('
                
                // If there is a function token at the top of the stack, pop it.
                if (!operatorStack.isEmpty() && DEFINITIONS.containsKey(operatorStack.peek())) {
                    outputQueue.add(operatorStack.pop());
                }
            }
            else {
                throw new IllegalArgumentException("Invalid token found during parsing: " + token);
            }
        }
        
        // 7. Pop remaining operators from the stack to the output queue.
        while (!operatorStack.isEmpty()) {
            String token = operatorStack.pop();
            if (token.equals("("))
                throw new IllegalArgumentException("Mismatched parentheses in expression: missing ')'");
            
            outputQueue.add(token);
        }
        
        return outputQueue;
    }


    // --- 6. RPN Evaluation ---
    
    /**
     * Evaluates a list of RPN tokens.
     */
    private static double evaluateRpn(List<String> rpnTokens) {
        Stack<Double> valueStack = new Stack<>();

        for (String token : rpnTokens) {
            // 1. If the token is a number or 'p', push the value onto the stack.
            if (isNumberOrConstant(token)) {
                if (token.equals("p")) {
                    valueStack.push(Math.PI);
                } else {
                    valueStack.push(Double.parseDouble(token));
                }
            }
            // 2. If the token is a function or operator.
            else if (DEFINITIONS.containsKey(token)) {
                OpFuncDef def = DEFINITIONS.get(token);
                
                if (def.arity == 2) { // Binary
                    if (valueStack.size() < 2) throw new IllegalArgumentException("Binary operator '" + def.name + "' requires two operands.");
                    double b = valueStack.pop();
                    double a = valueStack.pop();
                    valueStack.push(((BinaryFunction)def.function).apply(a, b));
                }
                else if (def.arity == 1) { // Unary (Prefix or Postfix)
                    if (valueStack.size() < 1) throw new IllegalArgumentException("Unary operator '" + def.name + "' requires one operand.");
                    double a = valueStack.pop();
                    valueStack.push(((UnaryFunction)def.function).apply(a));
                }
            }
            else {
                throw new IllegalArgumentException("Unknown token during evaluation: " + token);
            }

            // Check for math domain errors (NaN, Infinity)
            if (!valueStack.isEmpty() && (Double.isNaN(valueStack.peek()) || Double.isInfinite(valueStack.peek()))) {
                throw new ArithmeticException("Math domain error (e.g., log(-1)) or division by zero.");
            }
        }

        if (valueStack.size() != 1)
            throw new IllegalArgumentException("Invalid RPN expression (too many/few operands).");

        return valueStack.pop();
    }

    // --- 7. Main Execution Flow ---
    
    /**
     * Public method to calculate the result of an infix expression string.
     */
    public static double calculate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression cannot be empty.");
        }
        
        List<String> tokens = tokenize(expression);
        List<String> rpn = infixToRpn(tokens);
        
        // DEBUG: Print RPN
        // System.out.println("RPN: " + rpn); 
        
        return evaluateRpn(rpn);
    }
    
    // --- 8. Main CLI Loop ---
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Java CLI Calculator (Infix Mode) ---");
        System.out.println("Supported Operations:");
        System.out.println("  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)");
        System.out.println("  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S30)");
        System.out.println("  Unary (Postfix): ! (Factorial, e.g., 6!)");
        System.out.println("  Constant: p (PI)");
        System.out.println("\nNOTE: Use explicit multiplication (e.g., 2*(3) is correct).");
        System.out.println("Type 'exit' or 'quit' to end.\n");

        while (true) {
            System.out.print("Expression: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty() || input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                break;
            }

            try {
                double result = calculate(input);
                System.out.printf("Result: **%.10f**\n\n", result);
            } catch (IllegalArgumentException | ArithmeticException ex) {
                System.out.println("Error: Invalid expression. " + ex.getMessage() + "\n");
            } catch (Exception ex) {
                System.out.println("An unexpected error occurred: " + ex.getMessage() + "\n");
            }
        }

        System.out.println("Exiting calculator. Goodbye!");
        scanner.close();
    }
}