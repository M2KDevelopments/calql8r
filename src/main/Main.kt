import java.util.*
import kotlin.math.*

// --- 1. Operator and Function Definitions ---

// Define custom functional interfaces (type aliases for clarity)
typealias BinaryFunction = (a: Double, b: Double) -> Double
typealias UnaryFunction = (a: Double) -> Double

/**
 * Represents an operator or function with its properties for the Shunting-Yard algorithm.
 */
data class OpFuncDef(
    val name: String,
    val precedence: Int,
    val isLeftAssociative: Boolean,
    val arity: Int, // 1 for unary, 2 for binary
    val function: Function<*> // Can be BinaryFunction or UnaryFunction
)

// Global static map for all known operators and functions
private val DEFINITIONS = mutableMapOf<String, OpFuncDef>()

// --- 2. Function Implementations ---

// Binary Functions
private val power: BinaryFunction = { a, b -> a.pow(b) }

// Custom Root: a r b means b-th root of a. (Root degree is b)
private val root: BinaryFunction = { a, b ->
    if (a < 0 && abs(b % 2.0) < 1e-9) // Check if b is an even integer
        throw IllegalArgumentException("Cannot take even root of a negative number.")
    a.pow(1.0 / b)
}

// Unary Post-fix Factorial
private val factorial: UnaryFunction = { a ->
    if (a < 0 || abs(a % 1.0) > 1e-9)
        throw IllegalArgumentException("Factorial only defined for non-negative integers.")
    if (a == 0.0) 1.0 else (1..a.toInt()).fold(1.0) { acc, i -> acc * i }
}

// --- 3. Static Initialization Block ---

private fun initializeDefinitions() {
    if (DEFINITIONS.isNotEmpty()) return

    // Precedence: Higher number binds tighter

    // Postfix Unary (Highest Precedence)
    DEFINITIONS["!"] = OpFuncDef("!", 6, true, 1, factorial)

    // Prefix Unary Functions (Right Associative precedence 5)
    DEFINITIONS["S"] = OpFuncDef("S", 5, false, 1, ::sin)
    DEFINITIONS["s"] = OpFuncDef("s", 5, false, 1, ::sinh)
    DEFINITIONS["C"] = OpFuncDef("C", 5, false, 1, ::cos)
    DEFINITIONS["c"] = OpFuncDef("c", 5, false, 1, ::cosh)
    DEFINITIONS["T"] = OpFuncDef("T", 5, false, 1, ::tan)
    DEFINITIONS["t"] = OpFuncDef("t", 5, false, 1, ::tanh)
    DEFINITIONS["l"] = OpFuncDef("l", 5, false, 1, ::ln)    // Ln (Natural Log)
    DEFINITIONS["L"] = OpFuncDef("L", 5, false, 1, ::log10) // Log10

    // Binary Operators
    DEFINITIONS["^"] = OpFuncDef("^", 4, false, 2, power)  // Right Associative
    DEFINITIONS["r"] = OpFuncDef("r", 4, true, 2, root)    // Left Associative

    DEFINITIONS["*"] = OpFuncDef("*", 3, true, 2) { a, b -> a * b }
    DEFINITIONS["/"] = OpFuncDef("/", 3, true, 2) { a, b -> a / b }

    DEFINITIONS["+"] = OpFuncDef("+", 2, true, 2) { a, b -> a + b }
    // Internal token '_' for binary subtraction, distinguishing it from unary minus.
    DEFINITIONS["_"] = OpFuncDef("-", 2, true, 2) { a, b -> a - b }
}

// --- 4. Lexer/Tokenizer ---

/**
 * Converts the raw input string into a list of tokens, handling unary minus.
 */
private fun tokenize(expression: String): List<String> {
    // Ensure definitions are initialized
    initializeDefinitions()
    
    // 1. Preprocess: Insert spaces around tokens for easier splitting
    var processed = expression.replace("\\s+".toRegex(), "") // Remove existing spaces

    val separators = listOf("(", ")", "+", "*", "/", "^", "!", "r", "S", "s", "C", "c", "T", "t", "l", "L", "p")
    for (sep in separators) {
        processed = processed.replace(sep, " $sep ")
    }

    // Separate minus sign, which is handled specially
    processed = processed.replace("-", " - ")

    // Clean up multiple spaces and split
    val tokens = processed.split("\\s+".toRegex()).filter { it.isNotEmpty() }.toMutableList()

    // 2. Handle Unary Minus vs. Binary Minus
    val resultTokens = mutableListOf<String>()

    for (i in tokens.indices) {
        val token = tokens[i]
        
        if (token == "-") {
            // Check if the preceding token is an operand (number, constant, '!', or ')')
            val isPrecedingTokenOperand = resultTokens.isNotEmpty() &&
                (isNumberOrConstant(resultTokens.last()) || resultTokens.last() == "!" || resultTokens.last() == ")")
            
            if (i == 0 || !isPrecedingTokenOperand) {
                // Unary Minus: Merge it with the next token (e.g., "-5")
                if (i + 1 < tokens.size) {
                    tokens[i + 1] = token + tokens[i + 1]
                }
                // Skip the current token as it's merged into the next
            } else {
                // Binary Minus: Use the internal token '_'
                resultTokens.add("_")
            }
        } else {
            resultTokens.add(token)
        }
    }
    return resultTokens
}

private fun isNumberOrConstant(s: String): Boolean {
    if (s == "p") return true
    return s.toDoubleOrNull() != null
}

// --- 5. Shunting-Yard Algorithm (Infix to RPN) ---

/**
 * Converts a list of infix tokens to a list of RPN tokens using Shunting-Yard.
 */
private fun infixToRpn(infixTokens: List<String>): List<String> {
    val outputQueue = mutableListOf<String>()
    val operatorStack = Stack<String>()

    for (token in infixTokens) {
        // 1. Number or constant 'p'
        if (isNumberOrConstant(token)) {
            outputQueue.add(token)
        }
        // 2. Function or prefix unary operator (not '!')
        else if (DEFINITIONS.containsKey(token) && DEFINITIONS[token]!!.arity == 1 && token != "!" || token == "(") {
            operatorStack.push(token)
        }
        // 3. Binary operator ('_' is binary subtraction)
        else if (DEFINITIONS.containsKey(token) && DEFINITIONS[token]!!.arity == 2) {
            val currentDef = DEFINITIONS[token]!!
            
            while (operatorStack.isNotEmpty()) {
                val topToken = operatorStack.peek()
                if (topToken == "(") break

                val topDef = DEFINITIONS[topToken] ?: break
                
                // Check precedence and associativity
                val isHigherPrecedence = currentDef.precedence < topDef.precedence
                val isSamePrecedenceLeftAssoc = currentDef.precedence == topDef.precedence && currentDef.isLeftAssociative

                if (isHigherPrecedence || isSamePrecedenceLeftAssoc) {
                    outputQueue.add(operatorStack.pop())
                } else {
                    break
                }
            }
            operatorStack.push(token)
        }
        // 4. Postfix operator '!'
        else if (token == "!") {
             outputQueue.add(token)
        }
        // 5. If the token is ')'
        else if (token == ")") {
            while (operatorStack.isNotEmpty() && operatorStack.peek() != "(") {
                outputQueue.add(operatorStack.pop())
            }
            if (operatorStack.isEmpty())
                throw IllegalArgumentException("Mismatched parentheses in expression: missing '('")
            
            operatorStack.pop() // Pop the '('
            
            // Pop function if one is above '('
            if (operatorStack.isNotEmpty() && DEFINITIONS.containsKey(operatorStack.peek())) {
                outputQueue.add(operatorStack.pop())
            }
        }
        else {
            throw IllegalArgumentException("Invalid token found during parsing: $token")
        }
    }

    // 6. Pop remaining operators
    while (operatorStack.isNotEmpty()) {
        val token = operatorStack.pop()
        if (token == "(")
            throw IllegalArgumentException("Mismatched parentheses in expression: missing ')'")
        
        outputQueue.add(token)
    }
    
    return outputQueue
}

// --- 6. RPN Evaluation ---

/**
 * Evaluates a list of RPN tokens.
 */
private fun evaluateRpn(rpnTokens: List<String>): Double {
    val valueStack = Stack<Double>()

    for (token in rpnTokens) {
        // 1. Number or 'p'
        if (isNumberOrConstant(token)) {
            if (token == "p") {
                valueStack.push(PI)
            } else {
                valueStack.push(token.toDouble())
            }
        }
        // 2. Function or operator
        else if (DEFINITIONS.containsKey(token)) {
            val def = DEFINITIONS[token]!!
            
            if (def.arity == 2) { // Binary
                if (valueStack.size < 2) throw IllegalArgumentException("Binary operator '${def.name}' requires two operands.")
                val b = valueStack.pop()
                val a = valueStack.pop()
                valueStack.push((def.function as BinaryFunction)(a, b))
            }
            else if (def.arity == 1) { // Unary
                if (valueStack.isEmpty()) throw IllegalArgumentException("Unary operator '${def.name}' requires one operand.")
                val a = valueStack.pop()
                valueStack.push((def.function as UnaryFunction)(a))
            }
        }
        else {
            throw IllegalArgumentException("Unknown token during evaluation: $token")
        }

        // Check for math domain errors (NaN, Infinity)
        if (!valueStack.isEmpty() && (valueStack.peek().isNaN() || valueStack.peek().isInfinite())) {
            throw ArithmeticException("Math domain error (e.g., log(-1)) or division by zero.")
        }
    }

    if (valueStack.size != 1)
        throw IllegalArgumentException("Invalid RPN expression (too many/few operands).")

    return valueStack.pop()
}

// --- 7. Main Execution Flow ---

/**
 * Public method to calculate the result of an infix expression string.
 */
fun calculate(expression: String): Double {
    initializeDefinitions()
    if (expression.trim().isEmpty()) {
        throw IllegalArgumentException("Expression cannot be empty.")
    }
    
    val tokens = tokenize(expression)
    val rpn = infixToRpn(tokens)
    
    // RPN DEBUG: println("RPN: $rpn") 
    
    return evaluateRpn(rpn)
}

// --- 8. Main CLI Loop ---

fun main() {
    val scanner = Scanner(System.`in`)

    println("--- Kotlin CLI Calculator (Infix Mode) ---")
    println("Supported Operations:")
    println("  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)")
    println("  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S30)")
    println("  Unary (Postfix): ! (Factorial, e.g., 6!)")
    println("  Constant: p (PI)")
    println("\nNOTE: Use explicit multiplication (e.g., 2*(3) is correct).")
    println("Type 'exit' or 'quit' to end.\n")

    while (true) {
        print("Expression: ")
        val input = scanner.nextLine().trim()

        if (input.isEmpty() || input.equals("exit", ignoreCase = true) || input.equals("quit", ignoreCase = true)) {
            break
        }

        try {
            val result = calculate(input)
            println("Result: **${"%.10f".format(result)}**\n")
        } catch (ex: IllegalArgumentException) {
            println("Error: Invalid expression. ${ex.message}\n")
        } catch (ex: ArithmeticException) {
            println("Error: Math exception. ${ex.message}\n")
        } catch (ex: Exception) {
            println("An unexpected error occurred: ${ex.message}\n")
        }
    }

    println("Exiting calculator. Goodbye!")
    scanner.close()
}