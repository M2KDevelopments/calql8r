package main

import java.util.*
import kotlin.math.PI

object Main {
    private const val DECIMAL_POINT = "."
    private const val OPERATOR_ADD = "+"
    private const val OPERATOR_SUBSTRACT = "-"
    private const val OPERATOR_MULTPILY = "*"
    private const val OPERATOR_DIVIDE = "/"
    private const val OPERATOR_LOGx = "l"
    private const val OPERATOR_POW = "^"
    private const val OPERATOR_ROOT = "r"
    private const val OPERATOR_SIN = "S"
    private const val OPERATOR_SINH = "s"
    private const val OPERATOR_COS = "C"
    private const val OPERATOR_COSH = "c"
    private const val OPERATOR_TAN = "T"
    private const val OPERATOR_TANH = "t"
    private const val OPERATOR_LOG10 = "L"
    private const val OPERATOR_LN = "E"
    private const val OPERATOR_FACTORIAL = "!"
    private const val PERMUTATIONS = "Y"
    private const val COMBINATIONS = "Z"
    private val ERROR: ArrayList<*> = ArrayList<Any?>()


    private fun construct_numbers_from_string_of_integers(expression: ArrayList<*>?): ArrayList<*>? {
        var start = -1
        var end = 0
        val numbers = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
        for (i in expression!!.indices) {
            if (Arrays.binarySearch(numbers, expression[i].toString()) >= 0) {
                if (start == -1) start = i
                end = i
                if (end == expression.size - 1 && start != -1) {
                    try {
                        val number: String = java.lang.String.join("", expression.subList(start, end + 1))
                        val value = number.toInt()
                        expression.set(start, value)
                        for (j in start + 1..end) expression.set(j, "")
                    } catch (e: Exception) {
                        return ERROR
                    }
                }
            } else {
                if (start != -1) {
                    try {
                        val number: String = java.lang.String.join("", expression.subList(start, end + 1))
                        val value = number.toInt()
                        expression.set(start, value)
                        for (j in start + 1..end) expression.set(j, "")
                    } catch (e: Exception) {
                        return ERROR
                    }
                    start = -1
                }
            }
        }
        expression.removeIf { n: Any -> n === "" }
        return expression
    }

    private fun construct_decimal_numbers(expression: ArrayList<*>?): ArrayList<*>? {
        var i = 0
        while (i < expression!!.size) {
            if (expression[i].toString() == DECIMAL_POINT) {
                if (i == 0 && i == expression.size - 1) return null
                try {
                    val number = expression[i - 1].toString() + "." + expression[i + 1]
                    expression.set(i - 1, number.toDouble())
                    expression.removeAt(i)
                    expression.removeAt(i)
                } catch (e: Exception) {
                    return ERROR
                }
            } else {
                i++
            }
        }
        return expression
    }

    private fun convert_negative_numbers(expression: ArrayList<*>?): ArrayList<*>? {
        for (i in expression!!.indices) {
            if (expression[i].toString() == OPERATOR_SUBSTRACT) {
                if (i == expression.size - 1) return null
                if (expression[i + 1].toString() == OPERATOR_SUBSTRACT) {
                    // double negative
                    expression.set(i, OPERATOR_ADD)
                    expression.set(i + 1, "")
                } else if (i == 0 && expression[i + 1] is Double) {
                    expression.set(i, "")
                    expression.set(i + 1, expression[i + 1] as Double * -1)
                } else if (i > 0 && expression[i - 1] is Double && expression[i + 1] is Double) {
                    expression.set(i, "")
                    expression.set(i + 1, expression[i + 1] as Double * -1)
                }
            }
        }
        expression.removeIf { n: Any -> n === "" }
        return expression
    }

    private fun calculate_1_value_expression(
        expression: ArrayList<*>?,
        operation_symbol: String,
        direction: EnumFunctionValueDirection,
        MyMath: ICalculateFunctionOneValue
    ): ArrayList<*>? {
        var i = 0
        while (i < expression!!.size) {
            if (expression[i].toString() == operation_symbol) {
                if (i == 0 && direction == EnumFunctionValueDirection.LEFT) {
                    return ERROR
                }
                if (i == expression.size - 1 && direction == EnumFunctionValueDirection.RIGHT) {
                    return ERROR
                }
                if (expression[i - 1] !is Double && direction == EnumFunctionValueDirection.LEFT) {
                    return ERROR
                }
                if (expression[i + 1] !is Double && direction == EnumFunctionValueDirection.RIGHT) {
                    return ERROR
                }
                var result: Double
                if (direction == EnumFunctionValueDirection.LEFT) {
                    result = MyMath.calculation_function(expression[i - 1].toString().toDouble())
                    if (result == Double.MIN_VALUE) return ERROR
                    // Remove unnecessary elements and update value
                    expression.set(i - 1, result)
                    expression.removeAt(i)
                    i = i - 1
                } else if (direction == EnumFunctionValueDirection.RIGHT) {
                    result = MyMath.calculation_function(expression[i + 1].toString().toDouble())
                    if (result == Double.MIN_VALUE) return ERROR
                    // Remove unnecessary elements and update value
                    expression.set(i, result)
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
        expression: ArrayList<*>?,
        operation_symbol: String,
        MyMath: ICalculateFunctionTwoValues
    ): ArrayList<*>? {
        var i = 0
        while (i < expression!!.size) {
            val c = expression[i].toString()
            if (c == operation_symbol) {
                val prev_num = expression[i - 1].toString().toDouble()
                val next_num = expression[i + 1].toString().toDouble()
                val result = MyMath.calculation_function(prev_num, next_num)
                if (result == Double.MIN_VALUE) return ERROR
                // Remove unnecessary elements and update value
                expression.set(i - 1, result)
                expression.removeAt(i)
                expression.removeAt(i)
                i = i - 1
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

    private fun calculate_permutation(n: Double, r: Double): Double {
        return if (n < r || n < 0 || r < 0) Int.MIN_VALUE.toDouble() else calculate_factorial(n) / calculate_factorial(n - r)
    }

    private fun calculate_combinations(n: Double, r: Double): Double {
        return if (n < r || n < 0 || r < 0) Int.MIN_VALUE.toDouble() else calculate_factorial(n) / (calculate_factorial(
            r
        ) * calculate_factorial(n - r))
    }

    private fun calculate_math(expr: ArrayList<*>?): Double {
        return try {
            // factorial and nPr and nCr
            var expression = calculate_1_value_expression(expr, OPERATOR_FACTORIAL, EnumFunctionValueDirection.LEFT,
                ICalculateFunctionOneValue { num: Double ->
                    val value = calculate_factorial(num)
                    if (value == Double.MIN_VALUE) return@calculate_1_value_expression Double.MIN_VALUE
                    value
                })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_2_value_expressions(expression, PERMUTATIONS,
                ICalculateFunctionTwoValues { obj: Double, n: Double ->
                    calculate_permutation(
                        n
                    )
                })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_2_value_expressions(expression, COMBINATIONS,
                ICalculateFunctionTwoValues { obj: Double, n: Double ->
                    calculate_combinations(
                        n
                    )
                })
            if (expression === ERROR) return Double.MIN_VALUE

            // calculate trigonometry
            expression = calculate_1_value_expression(expression, OPERATOR_SIN, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { a: Double -> Math.sin(a) })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_1_value_expression(expression, OPERATOR_SINH, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { x: Double -> Math.sinh(x) })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_1_value_expression(expression, OPERATOR_COS, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { a: Double -> Math.cos(a) })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_1_value_expression(expression, OPERATOR_COSH, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { x: Double -> Math.cosh(x) })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_1_value_expression(expression, OPERATOR_TAN, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { a: Double -> Math.tan(a) })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_1_value_expression(expression, OPERATOR_TANH, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { x: Double -> Math.tanh(x) })
            if (expression === ERROR) return Double.MIN_VALUE

            // calulate logarithms
            expression = calculate_1_value_expression(expression, OPERATOR_LOG10, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { number: Double ->
                    Math.log(number) / Math.log(
                        10.0
                    )
                })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_1_value_expression(expression, OPERATOR_LN, EnumFunctionValueDirection.RIGHT,
                ICalculateFunctionOneValue { a: Double -> Math.log(a) })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_2_value_expressions(expression, OPERATOR_LOGx,
                ICalculateFunctionTwoValues { a: Double, b: Double ->
                    Math.log(a) / Math.log(
                        b
                    )
                })
            if (expression === ERROR) return Double.MIN_VALUE

            // calculate exponents and roots
            expression = calculate_2_value_expressions(expression, OPERATOR_POW,
                ICalculateFunctionTwoValues { a: Double, b: Double ->
                    Math.pow(
                        a,
                        b
                    )
                })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_2_value_expressions(expression, OPERATOR_ROOT,
                ICalculateFunctionTwoValues { a: Double, b: Double ->
                    Math.pow(
                        b,
                        1 / a
                    )
                })
            if (expression === ERROR) return Double.MIN_VALUE

            // calculate basic arithmetic
            expression = calculate_2_value_expressions(expression, OPERATOR_DIVIDE,
                ICalculateFunctionTwoValues { a: Double, b: Double -> a / b })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_2_value_expressions(expression, OPERATOR_MULTPILY,
                ICalculateFunctionTwoValues { a: Double, b: Double -> a * b })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_2_value_expressions(expression, OPERATOR_SUBSTRACT,
                ICalculateFunctionTwoValues { a: Double, b: Double -> a - b })
            if (expression === ERROR) return Double.MIN_VALUE
            expression = calculate_2_value_expressions(expression, OPERATOR_ADD,
                ICalculateFunctionTwoValues { a: Double, b: Double ->
                    java.lang.Double.sum(
                        a,
                        b
                    )
                })
            if (expression === ERROR) return Double.MIN_VALUE
            if (expression!!.size != 1) return Double.MIN_VALUE
            if (expression[0] !is Double) Double.MIN_VALUE else expression[0].toString().toDouble()
        } catch (e: Exception) {
            Double.MIN_VALUE
        }
    }

    private fun calculate_innermost_brackets(expression: ArrayList<*>?, MyMath: ICalculations): ArrayList<*>? {
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
                val bracket_expression = expression.subList(start, end) as ArrayList<*>
                val value = MyMath.calculate_math(bracket_expression)
                if (value == Double.MIN_VALUE) return ERROR
                expression.set(last_open_bracket, value)

                // remove all elements from last_open_bracket to first_close_bracket
                for (j in last_open_bracket + 1..first_close_bracket) {
                    expression.removeAt(last_open_bracket + 1)
                }
                return expression
            }
        }
        return expression
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size < 1) {
            println("PLEASE ADD AN EXPRESSION TO CALCULATE")
            return
        }
        var expression: ArrayList<*>? = ArrayList<Any?>()

        // construct expression for arguments e.g 1+1 +2 /4 *4
        // white spaces are automatically handled by joining each argument
        for (i in 1 until args.size) {
            for (j in 0 until args[i].length) {
                expression!!.add(Character.toString(args[i][j]))
            }
        }

        // create the number from string
        expression = construct_numbers_from_string_of_integers(expression)

        // calculate decimal numbers
        expression = construct_decimal_numbers(expression)
        if (expression === ERROR) {
            println("INVALID DECIMAL NUMBER FORMAT")
            return
        }

        // replace all PI symbols with value
        for (i in expression!!.indices) {
            val piExists = Arrays.binarySearch(arrayOf("π", "PI", "pi", "p"), expression[i].toString()) >= 0
            if (piExists) expression.set(i, PI)
        }

        // convert negative numbers
        expression = convert_negative_numbers(expression)
        if (expression === ERROR) {
            println("INVALID NEGATIVE NUMBER FORMAT")
            return
        }

        // Calculate inner bracket expressions
        do {
            expression = calculate_innermost_brackets(expression,
                ICalculations { obj: ArrayList<*>? -> calculate_math() })
            if (expression === ERROR) {
                println("MATH ERROR")
                return
            }
        } while (expression!!.contains("("))
        val value = calculate_math(expression)
        if (value == Double.MIN_VALUE) {
            println("MATH ERROR")
            return
        }
        println(value)
    }

    private enum class EnumFunctionValueDirection {
        LEFT, RIGHT
    }

    private interface ICalculateFunctionOneValue {
        fun calculation_function(num: Double): Double
    }

    private interface ICalculateFunctionTwoValues {
        fun calculation_function(num1: Double, num2: Double): Double
    }

    private interface ICalculations {
        fun calculate_math(expression: ArrayList<*>?): Double
    }
}