package main

import (
	"bytes"
	"errors"
	"fmt"
	"log"
	"math"
	"os"
	"strconv"
)

// Constants
const DECIMAL_POINT rune = '.'
const OPERATOR_ADD rune = '+'
const OPERATOR_SUBSTRACT rune = '-'
const OPERATOR_MULTPILY rune = '*'
const OPERATOR_DIVIDE rune = '/'
const OPERATOR_LOGx rune = 'l'
const OPERATOR_POW rune = '^'
const OPERATOR_ROOT rune = 'r'
const OPERATOR_SIN rune = 'S'
const OPERATOR_SINH rune = 's'
const OPERATOR_COS rune = 'C'
const OPERATOR_COSH rune = 'c'
const OPERATOR_TAN rune = 'T'
const OPERATOR_TANH rune = 't'
const OPERATOR_LOG10 rune = 'L'
const OPERATOR_LN rune = 'E'
const OPERATOR_FACTORIAL rune = '!'
const PI rune = 'p'
const BRACKET_OPEN rune = '('
const BRACKET_CLOSE rune = ')'
const FACTORIAL rune = '!'
const PERMUTATIONS rune = 'Y'
const COMBINATIONS rune = 'Z'

const NUMBER rune = 'n'
const NONE rune = ' '

const FUNCTION_VALUE_DIRECTION_RIGHT byte = 1
const FUNCTION_VALUE_DIRECTION_LEFT byte = 0

type Element struct {
	value        float64
	integers     int64
	chr          rune
	digit_length int
}

func construct_numbers_from_string_of_integers(expression []Element) ([]Element, error) {

	var start = -1
	var end = 0
	var numbers = []byte{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}

	for i := 0; i < len(expression); i++ {

		var c rune = expression[i].chr
		if bytes.ContainsRune(numbers, c) {
			if start == -1 {
				start = i
			}
			end = i
			if end == len(expression)-1 && start != -1 {
				var number = ""
				for j := start; j <= end; j++ {
					var str = string(expression[j].chr)
					number += str
				}
				var bitSize = 64
				var base = 10
				var value, err = strconv.ParseInt(number, base, bitSize)

				// Handle error
				if err != nil {

					return expression, err
				}

				expression[start].integers = value
				expression[start].value = float64(value)
				expression[start].chr = NUMBER
				for j := start + 1; j <= end; j++ {
					expression[j].chr = NONE
				}
			}
		} else {
			if start != -1 {
				var number = ""
				for j := start; j <= end; j++ {
					var str = string(expression[j].chr)
					number += str
				}
				var bitSize = 64
				var base = 10
				value, err := strconv.ParseInt(number, base, bitSize)

				// Handle error
				if err != nil {
					return expression, err
				}

				expression[start].integers = value
				expression[start].value = float64(value)
				expression[start].chr = NUMBER
				for j := start + 1; j <= end; j++ {
					expression[j].chr = NONE
				}
				start = -1
			}
		}
	}

	// Remove NONE elements
	list := []Element{}
	for i := 0; i < len(expression); i++ {
		if expression[i].chr != NONE {
			list = append(list, expression[i])
		}
	}
	return list, nil
}

func construct_decimal_numbers(expression []Element) ([]Element, error) {

	for i := 0; i < len(expression); i++ {
		if expression[i].chr == DECIMAL_POINT {

			if i == 0 && (i == len(expression)-1) {
				return expression, errors.New("MISPLACED DEMICAL")
			}

			if expression[i-1].chr != NUMBER || expression[i+1].chr != NUMBER {
				return expression, errors.New("INVALID DECIMAL NUMBER FORMAT")
			}

			var number = fmt.Sprintf("%d.%d", expression[i-1].integers, expression[i+1].integers)
			var value, err = strconv.ParseFloat(number, 64)
			expression[i-1].value = value
			expression[i-1].chr = NUMBER

			if err != nil {
				return expression, errors.New("ERROR DECIMAL NUMBER CONVERSION")
			}

			expression[i].chr = NONE
			expression[i+1].chr = NONE
		}
	}

	// Remove NONE elements
	list := []Element{}
	for i := 0; i < len(expression); i++ {
		if expression[i].chr != NONE {
			list = append(list, expression[i])
		}
	}
	return list, nil
}

func convert_negative_numbers(expression []Element) ([]Element, error) {
	for i := 0; i < len(expression); i++ {
		if expression[i].chr == OPERATOR_SUBSTRACT {
			if i == len(expression)-1 {
				return expression, errors.New("MISPLACED MINUS")
			}
			if expression[i+1].chr == OPERATOR_SUBSTRACT {
				// double negative
				expression[i].chr = OPERATOR_ADD
				expression[i+1].chr = NONE
			} else if i == 0 && expression[i+1].chr == NUMBER {
				expression[i].chr = NONE
				expression[i+1].value = expression[i+1].value * -1
			} else if i > 0 && expression[i-1].chr == NUMBER && expression[i+1].chr == NUMBER {
				expression[i].chr = NONE
				expression[i+1].value = expression[i+1].value * -1
			}
		}
	}
	// Remove NONE elements
	list := []Element{}
	for i := 0; i < len(expression); i++ {
		if expression[i].chr != NONE {
			list = append(list, expression[i])
		}
	}
	return list, nil
}

func calculate_1_value_expression(expression []Element, operation_symbol rune, direction byte, calculation_function func(float64) (float64, error)) ([]Element, error) {

	for i := 0; i < len(expression); i++ {
		if expression[i].chr == operation_symbol {
			if i == 0 && direction == FUNCTION_VALUE_DIRECTION_LEFT {
				return expression, nil
			}
			if i == len(expression)-1 && direction == FUNCTION_VALUE_DIRECTION_RIGHT {
				return expression, nil
			}
			if expression[i-1].chr == NUMBER && direction == FUNCTION_VALUE_DIRECTION_LEFT {
				return expression, nil
			}
			if expression[i+1].chr == NUMBER && direction == FUNCTION_VALUE_DIRECTION_RIGHT {
				return expression, nil
			}

			if direction == FUNCTION_VALUE_DIRECTION_LEFT {
				result, err := calculation_function(expression[i-1].value)
				if err != nil {
					return expression, err
				}
				// Remove unnecessary elements and update value
				expression[i-1].value = result
				expression[i-1].chr = NUMBER
				expression[i].chr = NONE

			} else if direction == FUNCTION_VALUE_DIRECTION_RIGHT {
				result, err := calculation_function(expression[i+1].value)
				if err != nil {
					return expression, err
				}
				// Remove unnecessary elements and update value
				expression[i].value = result
				expression[i].chr = NUMBER
				expression[i+1].chr = NONE
			}
		}
	}

	// Remove NONE elements
	list := []Element{}
	for i := 0; i < len(expression); i++ {
		if expression[i].chr != NONE {
			list = append(list, expression[i])
		}
	}
	return list, nil
}

func calculate_2_value_expressions(expression []Element, operation_symbol rune, calculation_function func(float64, float64) (float64, error)) ([]Element, error) {

	var prev_number_index = -1 // keep tracker of number/characters to ignore
	for i := 0; i < len(expression); i++ {
		if expression[i].chr == operation_symbol {

			if i == len(expression)-1 || i == 0 {
				return expression, errors.New("MATH OPERATOR ERROR")
			}

			// return expression, errors.New("FNX MATH ERROR")
			var prevNum = expression[i-1]
			var nextNum = expression[i+1]

			// One or both elements on either side of the function is not a number
			if prevNum.chr == NUMBER && nextNum.chr == NUMBER && prev_number_index == -1 {

				var result, err = calculation_function(prevNum.value, nextNum.value)
				if err != nil {
					return expression, err
				}

				// Remove unnecessary elements and update value
				prev_number_index = i - 1
				expression[prev_number_index].value = result
				expression[prev_number_index].chr = NUMBER
				expression[i].chr = NONE
				expression[i+1].chr = NONE
			} else if nextNum.chr == NUMBER && prev_number_index != -1 {
				prevNum = expression[prev_number_index]
				var result, err = calculation_function(prevNum.value, nextNum.value)
				if err != nil {
					return expression, err
				}

				// Remove unnecessary elements and update value
				expression[prev_number_index].value = result
				expression[prev_number_index].chr = NUMBER
				expression[i].chr = NONE
				expression[i+1].chr = NONE
			} else {
				// return error: Function Calculation of a Non-Number
				return expression, errors.New("FNX MATH ERROR")
			}

		} else if expression[i].chr != NONE {
			prev_number_index = -1
		}
	}

	// Remove NONE elements
	list := []Element{}
	for i := 0; i < len(expression); i++ {
		if expression[i].chr != NONE {
			list = append(list, expression[i])
		}
	}
	return list, nil
}

func calculate_factorial(number float64) (float64, error) {
	if number < 0 {
		return 0.0, errors.New("FACTORIAL OFF NEGATIVE NUMBER")
	}
	if number == 0.0 {
		return 1.0, nil
	}
	if number == 1.0 {
		return 1.0, nil
	}
	if number == 2.0 {
		return 2.0, nil
	}
	const FACTORIAL_LIMIT = 69
	if number > FACTORIAL_LIMIT {
		return 0, errors.New("FACTORIAL TOO LARGE")
	}
	var total = 1
	for n := 1; n <= int(number); n++ {
		total *= n
	}
	return float64(total), nil
}

func calculate_permutation(n float64, r float64) (float64, error) {
	if n < r || n < 0 || r < 0 {
		return 0.0, errors.New("Math Erorr")
	}
	var above, err = calculate_factorial(n)
	below, err := calculate_factorial(n - r)
	return (above / below), err
}

func calculate_combinations(n float64, r float64) (float64, error) {
	if n < r || n < 0 || r < 0 {
		return 0.0, errors.New("Math Erorr")
	}
	var above, err = calculate_factorial(n)
	_r, err := calculate_factorial(r)
	n_r, err := calculate_factorial(n - r)
	return above / (_r * n_r), err
}

func calculate_math(expr []Element) (float64, error) {

	// factorial and nPr and nCr
	var expression, err = calculate_1_value_expression(expr, OPERATOR_FACTORIAL, FUNCTION_VALUE_DIRECTION_LEFT, calculate_factorial)
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_2_value_expressions(expression, PERMUTATIONS, calculate_permutation)
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_2_value_expressions(expression, COMBINATIONS, calculate_combinations)
	if err != nil {
		return 0.0, err
	}

	// calculate trigonometry
	expression, err = calculate_1_value_expression(expression, OPERATOR_SIN, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Sin(f), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_1_value_expression(expression, OPERATOR_SINH, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Sinh(f), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_1_value_expression(expression, OPERATOR_COS, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Cos(f), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_1_value_expression(expression, OPERATOR_COSH, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Cosh(f), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_1_value_expression(expression, OPERATOR_TAN, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Tan(f), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_1_value_expression(expression, OPERATOR_TANH, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Tanh(f), nil })
	if err != nil {
		return 0.0, err
	}

	// calculate logarithms
	expression, err = calculate_1_value_expression(expression, OPERATOR_LOG10, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Log10(f), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_1_value_expression(expression, OPERATOR_LN, FUNCTION_VALUE_DIRECTION_RIGHT, func(f float64) (float64, error) { return math.Log(f), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_2_value_expressions(expression, OPERATOR_LOGx, func(a, b float64) (float64, error) { return math.Log(a) / math.Log(b), nil })
	if err != nil {
		return 0.0, err
	}

	// calculate exponents and roots
	expression, err = calculate_2_value_expressions(expression, OPERATOR_POW, func(a, b float64) (float64, error) { return math.Pow(a, b), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_2_value_expressions(expression, OPERATOR_ROOT, func(a, b float64) (float64, error) { return math.Pow(b, 1/a), nil })
	if err != nil {
		return 0.0, err
	}

	// calculate basic arithmetic
	expression, err = calculate_2_value_expressions(expression, OPERATOR_DIVIDE, func(a, b float64) (float64, error) { return (a / b), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_2_value_expressions(expression, OPERATOR_MULTPILY, func(a, b float64) (float64, error) { return (a * b), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_2_value_expressions(expression, OPERATOR_SUBSTRACT, func(a, b float64) (float64, error) { return (a - b), nil })
	if err != nil {
		return 0.0, err
	}

	expression, err = calculate_2_value_expressions(expression, OPERATOR_ADD, func(a, b float64) (float64, error) { return (a + b), nil })
	if err != nil {
		return 0.0, err
	}

	//  Error Checks
	if len(expression) != 1 {
		return 0.0, errors.New("FINAL ANSWER NOT CALCULATED")
	}

	if expression[0].chr != NUMBER {
		return 0.0, errors.New("VALUE MATH ERROR")
	}

	return expression[0].value, nil
}

func calculate_innermost_brackets(expression []Element, calculate_fn func([]Element) (float64, error)) ([]Element, error) {

	// counters
	var last_open_bracket = -1
	var first_close_bracket = -1
	var count_open_bracket = 0
	var count_close_bracket = 0

	for i := 0; i < len(expression); i++ {

		// count brackets
		if expression[i].chr == BRACKET_OPEN {
			last_open_bracket = i
			count_open_bracket += 1
		} else if expression[i].chr == BRACKET_CLOSE {
			count_close_bracket += 1
			if first_close_bracket == -1 {
				first_close_bracket = i
			}
		}

		// Syntax error
		if count_close_bracket > count_open_bracket {
			return expression, errors.New("BRACKETS MISMATCH")
		}

		// when the number of open brackets and closing brackets match.
		// 'last_open_bracket' is the start and 'first_close_bracket' is the end. for the calculation
		if count_open_bracket == count_close_bracket && first_close_bracket != -1 {
			var start = last_open_bracket + 1
			var end = first_close_bracket
			var bracket_expression = expression[start:end]
			var value, err = calculate_fn(bracket_expression)
			if err != nil {
				return expression, err
			}
			expression[last_open_bracket].chr = NUMBER
			expression[last_open_bracket].value = value

			// remove all elements from last_open_bracket to first_close_bracket
			list := []Element{}
			for j := 0; j < len(expression); j++ {
				if (j < last_open_bracket+1) || (j > first_close_bracket) {
					list = append(list, expression[j])
				}
			}
			return list, nil
		}
	}

	return expression, nil
}

func containsBrackets(expression []Element) bool {
	for i := 0; i < len(expression); i++ {
		if expression[i].chr == BRACKET_OPEN {
			return true
		}
	}
	return false
}

func main() {

	if len(os.Args) < 2 {
		log.Fatal("PLEASE ADD AN EXPRESSION TO CALCULATE")
	}

	// construct expression for agruments e.g 1+1 +2 /4 *4
	// whitesplaces are automatically handled by joining each argument
	expression := []Element{}

	// Get arguments from command line in expreesion
	for i := 1; i < len(os.Args); i++ {
		arg := os.Args[i]
		for j := 0; j < len(arg); j++ {
			if arg[j] != ' ' { // skip white space
				var ele Element
				ele.chr = rune(arg[j])
				ele.digit_length = 0
				ele.integers = 0
				ele.value = 0.0
				expression = append(expression, ele)
			}

		}
	}

	const MAX_ARRAY_SIZE = 200
	if len(expression) >= MAX_ARRAY_SIZE {
		log.Fatal("EXPRESSION TOO LONG")
	}

	expression, err := construct_numbers_from_string_of_integers(expression)
	if err != nil {
		log.Fatal("INVALID NUMBER FORMAT")
	}

	// calculate decimal numbers
	expression, err = construct_decimal_numbers(expression)
	if err != nil {
		log.Fatal("INVALID DECIMAL NUMBER FORMAT")
	}

	// replace all PI symbols with value
	for i := 0; i < len(expression); i++ {
		if expression[i].chr == PI || expression[i].chr == 'π' {
			expression[i].value = math.Pi
			expression[i].chr = NUMBER
			expression[i].integers = 3
		}
	}

	// convert negative numbers
	expression, err = convert_negative_numbers(expression)
	if err != nil {
		log.Fatal("INVALID NEGATIVE NUMBER FORMAT")
	}

	// while loop to check for brackets
	for containsBrackets(expression) {
		expression, err = calculate_innermost_brackets(expression, calculate_math)
		if err != nil {
			log.Fatal("BRACKET MATH ERROR")
		}
	}

	// Calculate the remaining expression
	value, err := calculate_math(expression)

	if err != nil {
		log.Fatal("MATH ERROR")
	}

	fmt.Println(value)
}
