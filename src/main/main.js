/**
 Make a function one letter
 So that we can put split each char into an array

        ---------------------
        // Sin - S
        // SinH - s
        // Cos - C
        // CosH - c
        // Tan - T
        // Tanh - h
        // EXP or E - ^
        // SQRT - 2r
        // CBRT - 3r
        // RT - r
        // Ln - l
        // Log - L
        // Pol( - P
        // Rec( - R
        // PI = 3.142...
         ---------------------
 * @param {string} expression 
 * @returns 
 */
function change_expression_to_array(expression) {
    expression = expression.replace(/\s/gmi, '')
    expression = expression.replace(/sinh/gmi, 's')
    expression = expression.replace(/cosh/gmi, 'c')
    expression = expression.replace(/tanh/gmi, 't')
    expression = expression.replace(/sin/gmi, 'S')
    expression = expression.replace(/cos/gmi, 'C')
    expression = expression.replace(/tan/gmi, 'T')
    expression = expression.replace(/EXP/gmi, '*10^')
    expression = expression.replace(/SQRT/gmi, '2r')
    expression = expression.replace(/CBRT/gmi, '3r')
    expression = expression.replace(/RT/gmi, 'r')
    expression = expression.replace(/Ln/gmi, 'E')
    expression = expression.replace(/Logx/gmi, 'l')
    expression = expression.replace(/Log/gmi, 'L')
    expression = expression.replace(/Pol/gmi, 'P')
    expression = expression.replace(/Rec/gmi, 'R')
    expression = expression.replace(/X/gmi, '*')
    expression = expression.replace(/PI/gmi, 'p')

    // Turn string expression into array - 13.25 * 2 = ['1', '3', '.', '2', '5', '*', '2'];
    return expression.split('');
}

/**
 * Convert the string numbers in an array in integers e.g ['1', '2', '.', '2', '4'] = [12, '.', '24']
 * @param {Array<string>} expression_as_array 
 */
function change_string_numbers_to_integers(expression_as_array = []) {
    const numberSet = new Set(['1', '2', '3', '4', '5', '6', '7', '8', '9', '0']);
    while (expression_as_array.includes('0') || expression_as_array.includes('1') || expression_as_array.includes('2') || expression_as_array.includes('3') || expression_as_array.includes('4') || expression_as_array.includes('5') || expression_as_array.includes('6') || expression_as_array.includes('7') || expression_as_array.includes('8') || expression_as_array.includes('9')) {
        let start = -1;

        let number = 0;
        for (let i in expression_as_array) {
            const ch = expression_as_array[i];
            if (numberSet.has(ch)) {
                if (start == -1) start = parseInt(i);

                // if the number is the last element
                if (start != -1 && i == expression_as_array.length - 1) {
                    const num = expression_as_array.slice(start).join("") // join the trailing numbers
                    expression_as_array.splice(start);
                    expression_as_array.push(parseInt(num));
                }
            } else {

                // skip if a number has not been found yet
                if (start == -1) continue;

                // calculate the number
                const end = parseInt(i);
                let multipler = 1;
                for (let j = end - 1; j >= start; j--) {
                    number += parseInt(expression_as_array[j]) * multipler;
                    multipler *= 10;
                }


                // replace the string numbers with a integer number
                const count = end - start;
                if (start == 0) {
                    expression_as_array.unshift(number);
                    expression_as_array.splice(1, count);
                } else if (start == (end - 1)) {
                    // just one number
                    expression_as_array[start] = parseInt(expression_as_array[start]);
                } else {
                    // split the array at the point of the first part of the number
                    const part1 = expression_as_array.slice(0, start)
                    const part2 = expression_as_array.slice(start);

                    // remove the calculated number
                    part2.splice(0, count);

                    // update array expression inserting the calculated whole number
                    expression_as_array = [...part1, number, ...part2];
                }

                // reset start position
                start = -1;
                break;

            }


        }

    }

    return expression_as_array;
}

/**
 * Convert the numbers around the decimal point into decimal numbers e.g [12, '.', '24'] = [12.24]
 * @param {Array} expression_as_array 
 */
function construct_decimal_numbers(expression_as_array = []) {
    while (expression_as_array.includes('.')) { 

        // check if decimal is in the wrong place
        const i = expression_as_array.indexOf('.')
        if ((i - 1 < 0) || (i + 1 > expression_as_array.length - 1)) throw new Error('Math Error: Could not parse decimal');
        const num1 = expression_as_array[i - 1]; // number before decimal
        const num2 = expression_as_array[i + 1]; // number after decimal

        if(isNaN(num1) || isNaN(num2)) throw new Error('Math Error: Could not parse decimal');

        const number = parseFloat(`${num1}.${num2}`); // e.g 134 . 1414  = 134.1414

        if (i == 0) {
            expression_as_array.unshift(number);
            expression_as_array.splice(1, 3);
        }
        else {

            // split the array at the point of the first part of the decimal
            const prev = i - 1;
            const part1 = expression_as_array.slice(0, prev)
            const part2 = expression_as_array.slice(prev);

            // remove the calculated number
            part2.splice(0, 3);

            // update array expression inserting the calculated decimal number
            expression_as_array = [...part1, number, ...part2];
        }

    }
    return expression_as_array;
}

/**
 * Calculate expression which require 2 numbers e.g 5*5 or 5+2 or 3^4 
 * if the right is NOT a number just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {string} function_symbol 
 * @param {Array} expression_as_array 
 * @param {string} errorMessage 
 */
function calculate_1_value_expressions(function_symbol, expression_as_array, errorMessage, calculateCallback) {

    let start = 0;
    while (expression_as_array.includes(function_symbol, start)) {
        const index = expression_as_array.indexOf(function_symbol, start);
        if (index == expression_as_array.length - 1) return new Error(errorMessage);
        start = index + 1;

        // left side is base and right side is exponent
        const num = expression_as_array[index + 1];

        // check if the left and right side elements are both numbers
        if (typeof num == 'number') {
            const number = calculateCallback(num);
            if (index == 0) {
                expression_as_array.unshift(number);
                expression_as_array.splice(1, 3);
            } else {
                const part1 = expression_as_array.slice(0, index);
                const part2 = expression_as_array.slice(index);
                part2.splice(0, 2);
                expression_as_array = [...part1, number, ...part2];
            }
        }
        // if the right is NOT a number just skip because it might be an expression
        // so we will calculate it later when the expression is calculate by other functions

    }

    return expression_as_array;
}

/**
 * Calculate expression which require 2 numbers e.g 5*5 or 5+2 or 3^4 
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {string} function_symbol 
 * @param {Array} expression_as_array 
 * @param {string} errorMessage 
 */
function calculate_2_value_expressions(function_symbol, expression_as_array, errorMessage, calculateCallback) {

    let start = 0;
    while (expression_as_array.includes(function_symbol, start)) {
        const index = expression_as_array.indexOf(function_symbol, start);
        if (index == 0) return new Error(errorMessage);
        if (index == expression_as_array.length - 1) return new Error(errorMessage);
        start = index + 1;

        // left side is base and right side is exponent
        const left = expression_as_array[index - 1];
        const right = expression_as_array[index + 1];

        // check if the left and right side elements are both numbers
        if (typeof left == 'number' && typeof right == 'number') {
            const number = calculateCallback(left, right);
            const prev = index - 1;
            if (prev == 0) {
                expression_as_array.unshift(number);
                expression_as_array.splice(1, 3);
            } else {
                const part1 = expression_as_array.slice(0, prev);
                const part2 = expression_as_array.slice(prev);
                part2.splice(0, 3);
                expression_as_array = [...part1, number, ...part2];
            }
        }
        // if left element and right are NOT both numbers just skip because it might be an express
        // so we will calculate it later when the expression is calculate by other functions

    }

    return expression_as_array;
}

/**
 * Calculate exponent ^ function = [4, '^', 2] = [16]
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_exp(expression_as_array) {
    return calculate_2_value_expressions('^', expression_as_array, 'Syntax Math Error: Exponent expression', (base, exp) => Math.pow(base, exp));
}

/**
 * Calculate root 'r' function = [2, 'r', 16] = SQRT16 = [4]
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_root(expression_as_array) {
    return calculate_2_value_expressions('r', expression_as_array, 'Syntax Math Error: root expression', (num1, num2) => Math.pow(num2, 1.0 / num1));
}


/**
 * Calculate add + function = [4, '+', 2] = [6]
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_add(expression_as_array) {
    return calculate_2_value_expressions('+', expression_as_array, 'Syntax Math Error: Adding expression', (num1, num2) => (num1 + num2));
}

/**
 * Calculate substract - function = [4, '-', 2] = [2]
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_substract(expression_as_array) {
    return calculate_2_value_expressions('-', expression_as_array, 'Syntax Math Error: Substracting expression', (num1, num2) => (num1 - num2));
}

/**
 * Calculate multiply * function = [4, '*', 2] = [8]
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_multiply(expression_as_array) {
    return calculate_2_value_expressions('*', expression_as_array, 'Syntax Math Error: Multiplying expression', (num1, num2) => (num1 * num2));
}


/**
 * Calculate divide / function = [4, '/', 2] = [2]
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_divide(expression_as_array) {
    return calculate_2_value_expressions('/', expression_as_array, 'Syntax Math Error: Dividing expression', (num1, num2) => parseFloat(num1 / (num2 * 1.0)));
}


/**
 * Calculate sin function 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_sin(expression_as_array) {
    return calculate_1_value_expressions('S', expression_as_array, 'Syntax Math Error: Sin expression', (num) => Math.sin(num));
}

/**
 * Calculate sinh function 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_sinh(expression_as_array) {
    return calculate_1_value_expressions('s', expression_as_array, 'Syntax Math Error: Sinh expression', (num) => Math.sinh(num));
}


/**
 * Calculate sin function 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_cos(expression_as_array) {
    return calculate_1_value_expressions('C', expression_as_array, 'Syntax Math Error: Sin expression', (num) => Math.sin(num));
}

/**
 * Calculate sinh function 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_cosh(expression_as_array) {
    return calculate_1_value_expressions('c', expression_as_array, 'Syntax Math Error: cosh expression', (num) => Math.cosh(num));
}


/**
 * Calculate sin function 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_tan(expression_as_array) {
    return calculate_1_value_expressions('T', expression_as_array, 'Syntax Math Error: Sin expression', (num) => Math.sin(num));
}

/**
 * Calculate sinh function 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_tanh(expression_as_array) {
    return calculate_1_value_expressions('t', expression_as_array, 'Syntax Math Error: tanh expression', (num) => Math.tanh(num));
}


/**
 * Calculate e natural 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_e(expression_as_array) {
    return calculate_1_value_expressions('e', expression_as_array, 'Syntax Math Error: e natural', (num) => Math.exp(num));
}

/**
 * Calculate ln natural 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_ln(expression_as_array) {
    return calculate_1_value_expressions('E', expression_as_array, 'Syntax Math Error: Ln', (num) => Math.log(num));
}

/**
 * Calculate log 10 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_log10(expression_as_array) {
    return calculate_1_value_expressions('L', expression_as_array, 'Syntax Math Error: log10 ', (num) => Math.log10(num));
}

/**
 * Calculate logx natural 
 * if the right is NOT a number it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 * @param {Array} expression_as_array 
 */
function calculate_log(expression_as_array) {
    // https://stackoverflow.com/questions/3019278/how-can-i-specify-the-base-for-math-log-in-javascript
    return calculate_2_value_expressions('l', expression_as_array, 'Syntax Math Error: Logx', (base, raised) => Math.log(raised) / Math.log(base));
}

function calculate_double_negatives(expression_as_array) {

    for (const index in expression_as_array) {
        const i = parseInt(index);
        if (i < expression_as_array.length - 1) {
            // when a double negative is found next to each other
            if (expression_as_array[i] == '-' && expression_as_array[i + 1] == '-') {

                //remove the double negatives and replace with '+'
                const array_expression = [...expression_as_array.slice(0, i), '+', ...expression_as_array.slice(i + 2)];
                return array_expression;
            }
        }
    }
    return expression_as_array;
}

/**
 * Performs a series of mathematical calculations on a given list of expressions.
 * It evaluates trigonometric functions (sin, sinh, cos, cosh, tan, tanh),
 * logarithmic functions (log10, ln, log), exponentials, roots, and basic
 * arithmetic operations (division, multiplication, subtraction, addition).
 * The calculations are done in sequential order as listed above.
 *
 * @param {Array} list - An array of mathematical expressions to be evaluated.
 * @returns {Array} - The modified array after performing all calculations.
 */

function math_calculation(list) {
    // calculate trig
    list = calculate_sin(list);
    list = calculate_sinh(list);
    list = calculate_cos(list);
    list = calculate_cosh(list);
    list = calculate_tan(list);
    list = calculate_tanh(list);

    // calulate logarithms
    list = calculate_log10(list);
    list = calculate_ln(list);
    list = calculate_log(list);

    // calculate exponents and roots
    list = calculate_e(list);
    list = calculate_exp(list);
    list = calculate_root(list);

    // calculate normal arthimatic
    list = calculate_divide(list);
    list = calculate_multiply(list);
    list = calculate_substract(list);
    list = calculate_add(list);

    list = calculate_double_negatives(list);
    return list;
}

/**
 * Evaluates the innermost bracketed expression in a list of mathematical symbols.
 * 
 * This function iterates over a list of mathematical symbols, identifies the innermost
 * bracketed expression, and uses a callback function to calculate its value. It assumes
 * balanced brackets within the expression. If an imbalance is detected, an error is thrown.
 * 
 * @param {Array} list - An array representing a mathematical expression with brackets.
 * @param {Function} callback_calculation - A function that takes the innermost expression
 *                                          and returns its calculated value.
 * @returns {Array} - The updated list after the innermost bracket expression is evaluated.
 * @throws {Error} - Throws an error if there are unmatched brackets in the expression.
 */

function calculate_innermost_brackets(list, callback_calculation) {
    // check for brackets and get details
    // let first_open_bracket = -1;
    let last_open_bracket = -1;
    let first_close_bracket = -1;
    // let last_close_bracket = -1;
    let count_open_bracket = 0;
    let count_close_bracket = 0;

    for (const i in list) {

        // count the brackets
        if (list[i] == '(') {
            last_open_bracket = parseInt(i);
            count_open_bracket++;
        } else if (list[i] == ')') {
            count_close_bracket++;
            if (first_close_bracket == -1) first_close_bracket = parseInt(i);
        }

        // if there are more close brackets than open ones mid-count error
        if (count_close_bracket > count_close_bracket) throw Error('Math Syntax Error: Brackets')


        // if the count of the opening and closing brackets match
        // found the indices of the open and close brackets to start calcalating from
        if (count_open_bracket == count_close_bracket && (first_close_bracket != -1)) {
            const inner_expresson = list.slice(last_open_bracket + 1, first_close_bracket);

            // if the inner expression is number a number 
            if (inner_expresson.length == 1 && typeof inner_expresson[0] == 'number') {
                const array_expression = [...list.slice(0, last_open_bracket), inner_expresson[0], ...list.slice(first_close_bracket + 1)];
                return array_expression;
            } else {
                const ans = callback_calculation(inner_expresson);
                const array_expression = [...list.slice(0, last_open_bracket), ...ans, ...list.slice(first_close_bracket + 1)]
                return array_expression;
            }

        }
    }

    if (count_open_bracket != count_close_bracket) throw Error('Math Syntax Error: Uneven Brackets')

    return list;
}

/**
 * Right a math expression that should be calculated
 * @param {String} expression 
 */
function calculate(expression) {
    let list = change_expression_to_array(expression);
    list = change_string_numbers_to_integers(list); // combine string numbers into one number
    list = construct_decimal_numbers(list); // construct decimal numbers

    // replace constants
    for(const i in list) {
        if(list[i] == 'p') list[i] = Math.PI;
    }

    // recursive calcalutions until answer is found.
    while (list.includes('(')) list = calculate_innermost_brackets(list, math_calculation);
    while (list.length > 1) list = math_calculation(list);
    return list[0];
}

console.log(calculate("PI"));