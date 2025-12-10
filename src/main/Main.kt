package main

import java.util.*
import kotlin.math.*

const val DECIMAL_POINT = "."
const val OPERATOR_ADD = "+"
const val OPERATOR_SUBSTRACT = "-"
const val OPERATOR_MULTPILY = "*"
const val OPERATOR_DIVIDE = "/"
const val OPERATOR_LOGx = "l"
const val OPERATOR_POW = "^"
const val OPERATOR_ROOT = "r"
const val OPERATOR_SIN = "S"
const val OPERATOR_SINH = "s"
const val OPERATOR_COS = "C"
const val OPERATOR_COSH = "c"
const val OPERATOR_TAN = "T"
const val OPERATOR_TANH = "t"
const val OPERATOR_LOG10 = "L"
const val OPERATOR_LN = "E"
const val OPERATOR_FACTORIAL = "!"
const val PERMUTATIONS = "Y"
const val COMBINATIONS = "Z"

private enum class EnumFunctionValueDirection {
    LEFT, RIGHT
}

private fun construct_numbers_from_string_of_integers(expression: ArrayList<Any>?): ArrayList<Any>? {
    var start = -1
    var end = 0
    val numbers = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    for (i in expression!!.indices) {
        if (Arrays.binarySearch(numbers, expression[i].toString()) >= 0) {
            if (start == -1) start = i
            end = i
            if (end == expression.size - 1 && start != -1) {
                try {
                    val number = expression.subList(start, end + 1).joinToString("")
                    val value = number.toInt()
                    expression[start] = value
                    for (j in start + 1..end) expression[j] = ""
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        } else {
            if (start != -1) {
                try {
                    val number = expression.subList(start, end + 1).joinToString("")
                    val value = number.toInt()
                    expression[start] = value
                    for (j in start + 1..end) expression[j] = ""
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
                start = -1
            }
        }
    }
    expression.removeIf { n: Any -> n === "" }
    return expression
}

private fun construct_decimal_numbers(expression: ArrayList<Any>?): ArrayList<Any>? {
    var i = 0
    while (i < expression!!.size) {
        if (expression[i].toString() == DECIMAL_POINT) {
            if (i == 0 && i == expression.size - 1) return null
            try {
                val number = expression[i - 1].toString() + "." + expression[i + 1]
                expression[i - 1] = number.toDouble()
                expression.removeAt(i)
                expression.removeAt(i)
            } catch (e: Exception) {
                return null
            }
        } else {
            i++
        }
    }
    return expression
}

private fun convert_negative_numbers(expression: ArrayList<Any>?): ArrayList<Any>? {
    for (i in expression!!.indices) {
        if (expression[i].toString() == OPERATOR_SUBSTRACT) {
            if (i == expression.size - 1) return null
            if (expression[i + 1].toString() == OPERATOR_SUBSTRACT) {
                // double negative
                expression[i] = OPERATOR_ADD
                expression[i + 1] = ""
            } else if (i == 0 && expression[i + 1] is Double) {
                expression[i] = ""
                expression[i + 1] = expression[i + 1] as Double * -1
            } else if (i > 0 && expression[i - 1] is Double && expression[i + 1] is Double) {
                expression[i] = ""
                expression[i + 1] = expression[i + 1] as Double * -1
            }
        }
    }
    expression.removeIf { n: Any -> n === "" }
    return expression
}

private fun calculate_1_value_expression(
    expression: ArrayList<Any>?,
    operation_symbol: String,
    direction: EnumFunctionValueDirection,
    calculation_function: (Double) -> Double?
): ArrayList<Any>? {
    var i = 0
    while (i < expression!!.size) {
        if (expression[i].toString() == operation_symbol) {
            if (i == 0 && direction == EnumFunctionValueDirection.LEFT) {
                return null
            }
            if (i == expression.size - 1 && direction == EnumFunctionValueDirection.RIGHT) {
                return null
            }
            if (expression[i - 1] !is Double && direction == EnumFunctionValueDirection.LEFT) {
                return null
            }
            if (expression[i + 1] !is Double && direction == EnumFunctionValueDirection.RIGHT) {
                return null
            }
            var result: Double
            if (direction == EnumFunctionValueDirection.LEFT) {
                result = calculation_function(expression[i - 1].toString().toDouble()) ?: return null
                // Remove unnecessary elements and update value
                expression[i - 1] = result
                expression.removeAt(i)
                i -= 1
            } else if (direction == EnumFunctionValueDirection.RIGHT) {
                result = calculation_function(expression[i + 1].toString().toDouble()) ?: return null
                // Remove unnecessary elements and update value
                expression[i] = result
                expression.removeAt(i + 1)
            } else {
                i++
            }
        } else {
            i++
        }
    }
    return expression
}

private fun calculate_2_value_expressions(
    expression: ArrayList<Any>?,
    operation_symbol: String,
    calculation_function: (Double, Double) -> Double?
): ArrayList<Any>? {
    var i = 0
    while (i < expression!!.size) {
        val c = expression[i].toString()
        if (c == operation_symbol) {
            val prevNum = expression[i - 1].toString().toDouble()
            val nextNum = expression[i + 1].toString().toDouble()
            val result = calculation_function(prevNum, nextNum) ?: return null
            // Remove unnecessary elements and update value
            expression[i - 1] = result
            expression.removeAt(i)
            expression.removeAt(i)
            i -= 1
        } else {
            i++
        }
    }
    return expression
}

private fun calculate_factorial(number: Double): Double {
    if (number < 0) return 0.0
    if (number == 0.0) return 1.0
    if (number == 1.0) return 1.0
    if (number == 2.0) return 2.0
    var total = 1
    var n = 1
    while (n <= number) {
        total *= n
        n++
    }
    return total.toDouble()
}

private fun calculate_permutation(n: Double, r: Double): Double? {
    return if (n < r || n < 0 || r < 0) null else calculate_factorial(n) / calculate_factorial(n - r)
}

private fun calculate_combinations(n: Double, r: Double): Double? {
    return if (n < r || n < 0 || r < 0) null else calculate_factorial(n) / (calculate_factorial(
        r
    ) * calculate_factorial(n - r))
}

private fun calculate_math(expr: ArrayList<Any>?): Double? {
    return try {
        // factorial and nPr and nCr
        var expression = calculate_1_value_expression(expr, OPERATOR_FACTORIAL, EnumFunctionValueDirection.LEFT) { num: Double ->calculate_factorial(num) }
        if (expression === null) return null

        expression = calculate_2_value_expressions(expression, PERMUTATIONS) { n: Double, r: Double -> calculate_permutation(n, r)}
        if (expression === null) return null

        expression = calculate_2_value_expressions(expression, COMBINATIONS) { n: Double, r: Double -> calculate_combinations(n, r)}
        if (expression === null) return null

        // calculate trigonometry
        expression = calculate_1_value_expression(expression, OPERATOR_SIN, EnumFunctionValueDirection.RIGHT) { a -> sin(a) }
        if (expression === null) return null
        expression = calculate_1_value_expression(expression, OPERATOR_SINH, EnumFunctionValueDirection.RIGHT) { x -> sinh(x) }
        if (expression === null) return null
        expression = calculate_1_value_expression(expression, OPERATOR_COS, EnumFunctionValueDirection.RIGHT) { a -> cos(a) }
        if (expression === null) return null
        expression = calculate_1_value_expression(expression, OPERATOR_COSH, EnumFunctionValueDirection.RIGHT) { x -> cosh(x) }
        if (expression === null) return null
        expression = calculate_1_value_expression(expression, OPERATOR_TAN, EnumFunctionValueDirection.RIGHT){ x -> tan(x) }
        if (expression === null) return null

        expression = calculate_1_value_expression(expression, OPERATOR_TANH, EnumFunctionValueDirection.RIGHT) { x -> tanh(x) }
        if (expression === null) return null

        // calculate logarithms
        expression = calculate_1_value_expression(expression, OPERATOR_LOG10, EnumFunctionValueDirection.RIGHT) { num -> log10(num) }
        if (expression === null) return null

        expression = calculate_1_value_expression(expression, OPERATOR_LN, EnumFunctionValueDirection.RIGHT) { a -> Math.log(a) }
        if (expression === null) return null

        expression = calculate_2_value_expressions(expression, OPERATOR_LOGx) { a, b -> ln(a) / ln(b) }
        if (expression === null) return null

        // calculate exponents and roots
        expression = calculate_2_value_expressions(expression, OPERATOR_POW) { a, b -> a.pow(b) }
        if (expression === null) return null
        expression = calculate_2_value_expressions(expression, OPERATOR_ROOT) { a, b -> b.pow(1.0 / a) }
        if (expression === null) return null

        // calculate basic arithmetic
        expression = calculate_2_value_expressions(expression, OPERATOR_DIVIDE) { a: Double, b: Double -> a / b }
        if (expression === null) return null
        expression = calculate_2_value_expressions(expression, OPERATOR_MULTPILY) { a: Double, b: Double -> a * b }
        if (expression === null) return null
        expression = calculate_2_value_expressions(expression, OPERATOR_SUBSTRACT) { a: Double, b: Double -> a - b }
        if (expression === null) return null
        expression = calculate_2_value_expressions(expression, OPERATOR_ADD) { a: Double, b: Double -> a + b }

        if (expression === null) return null

        if (expression.size != 1) return null

        if (expression[0] !is Double) null else expression[0].toString().toDouble()

    } catch (e: Exception) {
        null
    }
}

private fun calculate_innermost_brackets(expression: ArrayList<Any>?, calculate_fn:(expr:ArrayList<Any>?)->Double?): ArrayList<Any>? {
    var last_open_bracket = -1
    var first_close_bracket = -1
    var count_open_bracket = 0
    var count_close_bracket = 0
    for (i in expression!!.indices) {
        if (expression[i].toString() == "(") {
            last_open_bracket = i
            count_open_bracket += 1
        } else if (expression[i].toString() == ")") {
            count_close_bracket += 1
            if (first_close_bracket == -1) {
                first_close_bracket = i
            }
        }

        // Syntax error
        if (count_close_bracket > count_close_bracket) return null

        // when the number of open brackets and closing brackets match.
        // 'last_open_bracket' is the start and 'first_close_bracket' is the end. for the calculation
        if (count_open_bracket == count_close_bracket &&
            first_close_bracket != -1
        ) {
            val start = last_open_bracket + 1
            val end = first_close_bracket
            val bracket_expression = expression.subList(start, end) as ArrayList<Any>
            val value = calculate_fn(bracket_expression) ?: return null
            expression[last_open_bracket] = value

            // remove all elements from last_open_bracket to first_close_bracket
            for (j in last_open_bracket + 1..first_close_bracket) {
                expression.removeAt(last_open_bracket + 1)
            }
            return expression
        }
    }
    return expression
}

fun main(args: Array<String>) {

    if (args.size <= 1) {
        println("PLEASE ADD AN EXPRESSION TO CALCULATE")
        return
    }
    var expression:ArrayList<Any>? = ArrayList<Any>()

    // construct expression for arguments e.g 1+1 +2 /4 *4
    // white spaces are automatically handled by joining each argument
    for (i in 1 until args.size) {
        for (j in 0 until args[i].length) {
            expression?.add(args[i][j].toString())
        }
    }

    // create the number from string
    expression = construct_numbers_from_string_of_integers(expression)
    if (expression === null) {
        println("INVALID NUMBER FORMAT")
        return
    }

    // calculate decimal numbers
    expression = construct_decimal_numbers(expression)
    if (expression === null) {
        println("INVALID DECIMAL NUMBER FORMAT")
        return
    }

    // replace all PI symbols with value
    for (i in expression.indices) {
        val piExists = Arrays.binarySearch(arrayOf("π", "PI", "pi", "p"), expression[i].toString()) >= 0
        if (piExists) expression[i] = PI
    }

    // convert negative numbers
    expression = convert_negative_numbers(expression)
    if (expression === null) {
        println("INVALID NEGATIVE NUMBER FORMAT")
        return
    }

    // Calculate inner bracket expressions
    do {
        expression = calculate_innermost_brackets(expression) {expr -> calculate_math(expr) }
        if (expression === null) {
            println("MATH ERROR")
            return
        }
    } while (expression!!.contains("("))
    val value = calculate_math(expression)
    if (value == null) {
        println("MATH ERROR")
        return
    }
    println(value)
}

