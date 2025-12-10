import kotlin.math.*

class Parser(private var text: String) {
    private var pos = 0

    init {
        text = text.replace(" ", "")
    }

    private fun peek(): Char = if (pos < text.length) text[pos] else '\u0000'
    private fun next(): Char = if (pos < text.length) text[pos++] else '\u0000'
    private fun eat(c: Char): Boolean {
        if (peek() == c) {
            pos++
            return true
        }
        return false
    }

    // ---------------- Expression → Term ((+|-) Term)*
    fun parseExpression(): Double {
        var value = parseTerm()
        while (true) {
            when {
                eat('+') -> value += parseTerm()
                eat('-') -> value -= parseTerm()
                else -> return value
            }
        }
    }

    // ---------------- Term → Factor ((*|/) Factor)*
    private fun parseTerm(): Double {
        var value = parseFactor()
        while (true) {
            when {
                eat('*') -> value *= parseFactor()
                eat('/') -> value /= parseFactor()
                else -> return value
            }
        }
    }

    // ---------------- Factor → Unary (^ Unary | r Unary)*
    private fun parseFactor(): Double {
        var value = parseUnary()
        while (true) {
            when {
                eat('^') -> value = value.pow(parseUnary())
                eat('r') -> { val b = parseUnary(); value = b.pow(1.0 / value) }
                else -> return value
            }
        }
    }

    // ---------------- Unary → (+|-) Unary | Primary
    private fun parseUnary(): Double {
        return when {
            eat('+') -> parseUnary()
            eat('-') -> -parseUnary()
            else -> parsePrimary()
        }
    }

    // ---------------- Primary → number | function | '(' expression ')' | factorial
    private fun parsePrimary(): Double {
        val c = peek()

        // Function
        if (c.isLetter()) {
            val fn = readFunction()
            val arg = parsePrimary()
            return applyFunction(fn, arg)
        }

        // Parentheses
        if (eat('(')) {
            val value = parseExpression()
            if (!eat(')')) error("Missing )")
            if (eat('!')) return factorial(value)
            return value
        }

        // Number or constant
        val number = parseNumber()
        if (eat('!')) return factorial(number)
        return number
    }

    // Read function letters (S, s, C, c, T, h, l, L, r)
    private fun readFunction(): String {
        val sb = StringBuilder()
        while (peek().isLetter()) sb.append(next())
        return sb.toString()
    }

    // Parse number OR π constant
    private fun parseNumber(): Double {
        if (peek() == 'p') {
            next()
            return Math.PI
        }

        val sb = StringBuilder()
        while (peek().isDigit() || peek() == '.') sb.append(next())

        if (sb.isEmpty()) error("Number expected")

        return sb.toString().toDouble()
    }

    private fun applyFunction(fn: String, x: Double): Double {
        return when (fn) {
            "S" -> sin(x)
            "s" -> sinh(x)
            "C" -> cos(x)
            "c" -> cosh(x)
            "T" -> tan(x)
            "h" -> tanh(x)
            "l" -> ln(x)
            "L" -> log10(x)
            "r" -> sqrt(x)
            else -> error("Unknown function: $fn")
        }
    }

    private fun factorial(n: Double): Double {
        require(n >= 0) { "Factorial of negative number" }
        var r = 1.0
        for (i in 2..n.toInt()) r *= i
        return r
    }
}

fun main() {
    println("Kotlin CLI Scientific Calculator (type 'exit' to quit)")

    while (true) {
        print("> ")
        val input = readLine() ?: continue

        if (input.lowercase() == "exit") break

        try {
            val parser = Parser(input)
            val result = parser.parseExpression()
            println("= $result")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }
}
