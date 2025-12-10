import kotlin.math.*

fun factorial(n: Double): Double {
    if (n < 0) throw IllegalArgumentException("Factorial not defined for negative numbers")
    if (n == 0.0 || n == 1.0) return 1.0
    
    var result = 1.0
    for (i in 2..n.toInt()) {
        result *= i
    }
    return result
}

fun evaluateExpression(input: String): Double {
    var expr = input.replace(" ", "")
    
    // Replace p with pi
    expr = expr.replace("p", PI.toString())
    
    // Handle factorials (n!)
    val factorialRegex = """(\d+\.?\d*)!""".toRegex()
    expr = factorialRegex.replace(expr) { matchResult ->
        val num = matchResult.groupValues[1].toDouble()
        factorial(num).toString()
    }
    
    // Handle two-value functions (aFb format)
    
    // Pow (^): a^b
    val powRegex = """(\d+\.?\d*)\^(\d+\.?\d*)""".toRegex()
    expr = powRegex.replace(expr) { matchResult ->
        val a = matchResult.groupValues[1].toDouble()
        val b = matchResult.groupValues[2].toDouble()
        a.pow(b).toString()
    }
    
    // Root (r): arb means b-th root of a
    val rootRegex = """(\d+\.?\d*)r(\d+\.?\d*)""".toRegex()
    expr = rootRegex.replace(expr) { matchResult ->
        val a = matchResult.groupValues[1].toDouble()
        val b = matchResult.groupValues[2].toDouble()
        a.pow(1.0 / b).toString()
    }
    
    // Handle one-value functions (Fn format)
    
    // Sin - S
    val sinRegex = """S(\d+\.?\d*)""".toRegex()
    expr = sinRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        sin(n).toString()
    }
    
    // SinH - s
    val sinhRegex = """s(\d+\.?\d*)""".toRegex()
    expr = sinhRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        sinh(n).toString()
    }
    
    // Cos - C
    val cosRegex = """C(\d+\.?\d*)""".toRegex()
    expr = cosRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        cos(n).toString()
    }
    
    // CosH - c
    val coshRegex = """c(\d+\.?\d*)""".toRegex()
    expr = coshRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        cosh(n).toString()
    }
    
    // Tan - T
    val tanRegex = """T(\d+\.?\d*)""".toRegex()
    expr = tanRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        tan(n).toString()
    }
    
    // Tanh - t
    val tanhRegex = """t(\d+\.?\d*)""".toRegex()
    expr = tanhRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        tanh(n).toString()
    }
    
    // Ln - l
    val lnRegex = """l(\d+\.?\d*)""".toRegex()
    expr = lnRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        ln(n).toString()
    }
    
    // Log (base 10) - L
    val logRegex = """L(\d+\.?\d*)""".toRegex()
    expr = logRegex.replace(expr) { matchResult ->
        val n = matchResult.groupValues[1].toDouble()
        log10(n).toString()
    }
    
    // Evaluate the final expression using a simple expression evaluator
    return evaluateBasicExpression(expr)
}

fun evaluateBasicExpression(expr: String): Double {
    // Simple recursive descent parser for +, -, *, /, ()
    return Parser(expr).parseExpression()
}

class Parser(private val expr: String) {
    private var pos = 0
    
    private fun currentChar(): Char? = if (pos < expr.length) expr[pos] else null
    
    private fun advance() {
        pos++
    }
    
    private fun skipWhitespace() {
        while (currentChar()?.isWhitespace() == true) advance()
    }
    
    fun parseExpression(): Double {
        var result = parseTerm()
        
        while (true) {
            skipWhitespace()
            when (currentChar()) {
                '+' -> {
                    advance()
                    result += parseTerm()
                }
                '-' -> {
                    advance()
                    result -= parseTerm()
                }
                else -> break
            }
        }
        
        return result
    }
    
    private fun parseTerm(): Double {
        var result = parseFactor()
        
        while (true) {
            skipWhitespace()
            when (currentChar()) {
                '*' -> {
                    advance()
                    result *= parseFactor()
                }
                '/' -> {
                    advance()
                    result /= parseFactor()
                }
                else -> break
            }
        }
        
        return result
    }
    
    private fun parseFactor(): Double {
        skipWhitespace()
        
        // Handle negative numbers
        if (currentChar() == '-') {
            advance()
            return -parseFactor()
        }
        
        // Handle positive sign
        if (currentChar() == '+') {
            advance()
            return parseFactor()
        }
        
        // Handle parentheses
        if (currentChar() == '(') {
            advance()
            val result = parseExpression()
            skipWhitespace()
            if (currentChar() == ')') {
                advance()
            } else {
                throw IllegalArgumentException("Expected ')'")
            }
            return result
        }
        
        // Handle numbers
        return parseNumber()
    }
    
    private fun parseNumber(): Double {
        skipWhitespace()
        val start = pos
        
        while (currentChar()?.isDigit() == true || currentChar() == '.') {
            advance()
        }
        
        if (start == pos) {
            throw IllegalArgumentException("Expected number at position $pos")
        }
        
        return expr.substring(start, pos).toDouble()
    }
}

fun main() {
    println("=".repeat(50))
    println("CLI Calculator")
    println("=".repeat(50))
    println("\nOperators:")
    println("  S - Sin      s - SinH")
    println("  C - Cos      c - CosH")
    println("  T - Tan      t - TanH")
    println("  ^ - Power    r - Root (arb = b-th root of a)")
    println("  l - Ln       L - Log (base 10)")
    println("  ! - Factorial")
    println("  p - Pi (3.14159...)")
    println("\nExamples:")
    println("  1+2")
    println("  2+5^4")
    println("  2*(2+25)")
    println("  6!")
    println("  2+4^2")
    println("  8r3 (cube root of 8)")
    println("  L100 (log base 10 of 100)")
    println("  S1.57 (sin of 1.57)")
    println("\nType 'quit' or 'exit' to exit\n")
    println("=".repeat(50))
    
    while (true) {
        try {
            print("\nEnter expression: ")
            val input = readLine()?.trim() ?: ""
            
            // Check for exit commands
            if (input.lowercase() in listOf("quit", "exit", "q")) {
                println("Goodbye!")
                break
            }
            
            // Skip empty input
            if (input.isEmpty()) continue
            
            // Evaluate and display result
            val result = evaluateExpression(input)
            println("Result: $result")
            
        } catch (e: IllegalArgumentException) {
            println("Error: ${e.message}")
        } catch (e: Exception) {
            println("Unexpected error: ${e.message}")
        }
    }
}