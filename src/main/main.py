import sys
import math
import numbers
from typing import Callable

FUNCTION_VALUE_DIRECTION_RIGHT = 1
FUNCTION_VALUE_DIRECTION_LEFT = -1
DECIMAL_POINT= '.'

OPERATOR_ADD= '+'
OPERATOR_SUBSTRACT= '-'
OPERATOR_MULTPILY= '*'
OPERATOR_DIVIDE= '/'
OPERATOR_LOGx = 'l'
OPERATOR_POW= '^'
OPERATOR_ROOT= 'r'
OPERATOR_SIN= 'S'
OPERATOR_SINH= 's'
OPERATOR_COS= 'C'
OPERATOR_COSH= 'c'
OPERATOR_TAN= 'T'
OPERATOR_TANH= 't'
OPERATOR_LOG10= 'L'
OPERATOR_LN= 'E'
OPERATOR_FACTORIAL= '!'
# PI= 'p'
# BRACKET_OPEN= '('
# BRACKET_CLOSE= ')'
FACTORIAL= '!'
PERMUTATIONS= 'Y'
COMBINATIONS= 'Z'

def construct_numbers_from_string_of_integers(expression:list):
    start = -1
    end = 0
    for i in range(0, len(expression)):
        if expression[i] in ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9']:
            if start == -1:
                start = i
            end = i
            if (end == len(expression) - 1) and (start != -1):
                try:
                    num = int("".join(expression[start: end+1]))
                    expression[start] = num
                    for j in range(start+1, end+1):
                        expression[j] = ''
                    start = -1
                except:
                    return None
        else:
            if start != -1:
                try:
                    num = int("".join(expression[start: end+1]))
                    expression[start] = num
                    for j in range(start + 1, end+1):
                        expression[j] = ''
                    start = -1
                except:
                    return None

    # remove all blank space
    for i in range(expression.count('')):
        expression.remove('')

    return expression


def construct_decimal_numbers(expression: list):
    i = 0
    while i < len(expression):
        if expression[i] == DECIMAL_POINT:
            # boundary check for decimal point
            if i == 0 or i == len(expression) - 1:
                return None
            try:
                expression[i-1] = float(f"{expression[i-1]}.{expression[i+1]}")
                expression.pop(i)  # decimal point
                expression.pop(i)  # next number
            except:
                return None
        else:
            i += 1
    return expression


def convert_negative_numbers(expression: list):
    # e.g -1+2, 2*(-1), -2*-1/2--1*(-3-2) (1-2)-13
    for i in range(0, len(expression)):
        if expression[i] == OPERATOR_SUBSTRACT:
            if i == len(expression) - 1:  # boundary check for minus
                return None
            if (expression[i+1] == OPERATOR_SUBSTRACT):  # double negative
                expression[i] = OPERATOR_ADD
                expression[i+1] = ''
            elif i == 0 and isinstance(expression[i+1], numbers.Number):
                expression[i] = ''
                expression[i+1] *= -1
            elif i > 0 and isinstance(expression[i-1], numbers.Number) == False and isinstance(expression[i+1], numbers.Number):
                expression[i] = ''
                expression[i+1] *= -1

    # remove all blank space
    for i in range(expression.count('')):
        expression.remove('')
    return expression


def calculate_1_value_expression(expression: list, operation_symbol: str, direction: int, calculation_function: Callable[[numbers.Number], numbers.Number]) -> list:
    i = 0
    while i < len(expression):
        if expression[i] == operation_symbol:
            # boundary check for operation symbol
            if i == 0 and direction == FUNCTION_VALUE_DIRECTION_LEFT:
                return None
            
            if (i == (len(expression) - 1)) and direction == FUNCTION_VALUE_DIRECTION_RIGHT :
                return None

            if isinstance(expression[i-1], numbers.Number) == False and direction == FUNCTION_VALUE_DIRECTION_LEFT:
                return None
            
            if isinstance(expression[i+1], numbers.Number) == False and direction == FUNCTION_VALUE_DIRECTION_RIGHT:
                return None

            result = None
            if direction == FUNCTION_VALUE_DIRECTION_LEFT:
                result = calculation_function(expression[i-1])
                
                if result == None or isinstance(result, numbers.Number) == False:
                    return None
                
                # Remove unnecessary elements and update value
                expression[i-1] = result
                expression.pop(i)
                i = i-1
            elif direction == FUNCTION_VALUE_DIRECTION_RIGHT:
                result = calculation_function(expression[i+1])
     
                if result == None or isinstance(result, numbers.Number) == False:
                    return None

                # Remove unnecessary elements and update value
                expression[i] = result
                expression.pop(i+1)
            else:
                i += 1
        else:
            i += 1
    return expression


def calculate_2_value_expressions(expression: list, operation_symbol: str, calculation_function: Callable[[numbers.Number, numbers.Number], numbers.Number]) -> list:
    i = 0
    while i < len(expression):
        if expression[i] == operation_symbol:
            # boundary check for operation symbol
            if i == 0 or i == len(expression) - 1:
                return None

            if isinstance(expression[i-1], numbers.Number) == False or isinstance(expression[i+1], numbers.Number) == False:
                return None

            result = calculation_function(expression[i-1], expression[i+1])
            if result == None or isinstance(result, numbers.Number) == False:
                return None

            expression[i-1] = result
            expression.pop(i)
            expression.pop(i)
            i = i-1
        else:
            i += 1
    return expression


def calculate_math(expression:list) -> numbers.Number:
    try:
        
        # factorial and nPr and nCr 
        expression =  calculate_1_value_expression(expression, OPERATOR_FACTORIAL, FUNCTION_VALUE_DIRECTION_LEFT, lambda num: math.factorial(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_2_value_expressions(expression, PERMUTATIONS, lambda n, r: math.perm(n, r))
        if(expression == None) : 
            return None
        

        expression =  calculate_2_value_expressions(expression, COMBINATIONS, lambda n, r: math.comb(n, r))
        if(expression == None) : 
            return None

        # calculate trigonometry
        
        expression =  calculate_1_value_expression(expression, OPERATOR_SIN, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.sin(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_1_value_expression(expression, OPERATOR_SINH, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.sinh(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_1_value_expression(expression, OPERATOR_COS, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.cos(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_1_value_expression(expression, OPERATOR_COSH, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.cosh(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_1_value_expression(expression, OPERATOR_TAN, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.tan(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_1_value_expression(expression, OPERATOR_TANH, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.tanh(num))
        if(expression == None) : 
            return None

        
        # calulate logarithms
        expression =  calculate_1_value_expression(expression, OPERATOR_LOG10, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.log10(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_1_value_expression(expression, OPERATOR_LN, FUNCTION_VALUE_DIRECTION_RIGHT, lambda num: math.log(num))
        if(expression == None) : 
            return None
        
        expression =  calculate_2_value_expressions(expression, OPERATOR_LOGx, lambda a, b: math.log(b, a))
        if(expression == None) : 
            return None

        
        # calculate exponents and roots    
        expression =  calculate_2_value_expressions(expression, OPERATOR_POW, lambda base, exp: math.pow(base, exp))
        if(expression == None) : 
            return None
        
        expression =  calculate_2_value_expressions(expression, OPERATOR_ROOT, lambda a, b: math.pow(b, 1/a))
        if(expression == None) : 
            return None
    
        # calculate basic arithmitic    
        expression = calculate_2_value_expressions(expression, OPERATOR_DIVIDE, lambda a, b: a / b)
        if(expression == None) : 
            return None
        
        expression =  calculate_2_value_expressions(expression, OPERATOR_MULTPILY, lambda a, b: a * b)
        if(expression == None) : 
            return None
        
        expression =  calculate_2_value_expressions(expression, OPERATOR_SUBSTRACT, lambda a, b: a - b)
        if(expression == None) : 
            return None
        

        expression =  calculate_2_value_expressions(expression, OPERATOR_ADD, lambda a, b: a + b)
        if(expression == None) : 
            return None
        
        if(len(expression) != 1):
            return None
        if(isinstance(expression[0], numbers.Number) == False):
            return None
        
        
        value : numbers.Number = expression[0]
        return value
    except:
        return None


def calculate_innermost_brackets(expression:list, calculate_math: Callable[[list], numbers.Number]= calculate_math) -> list:
    last_open_bracket = -1
    first_close_bracket = -1
    count_open_bracket = 0
    count_close_bracket = 0
    for i in range(0, len(expression)):
        if expression[i] == '(':
            last_open_bracket = i
            count_open_bracket += 1
        elif expression[i] == ')':
            count_close_bracket += 1
            if (first_close_bracket == -1):
                first_close_bracket = i

        if (count_close_bracket > count_close_bracket):
            return None

        # when the number of open brackets and closing brackets match.
        # 'last_open_bracket' is the start and 'first_close_bracket' is the end. for the calculation
        if (count_open_bracket == count_close_bracket and (first_close_bracket != -1)):

            # generate expression that is found in inner most brackets
            bracket_expression = expression[last_open_bracket +
                                            1: first_close_bracket]
            value = calculate_math(bracket_expression)
            if value == None:
                return None
            
            expression[last_open_bracket] = value

            # remove all elements from last_open_bracket to first_close_bracket
            for j in range(last_open_bracket + 1, first_close_bracket+1):
                index = last_open_bracket + 1
                expression.pop(index)

            return expression

    return expression


def main():


    if len(sys.argv) < 1:
        return print("PLEASE ADD AN EXPRESSION TO CALCULATE")


    # construct expression for agruments e.g 1+1 +2 /4 *4
    # whitesplaces are automatically handled by joining each argument
    expression = []
    for i in range(1, len(sys.argv)):
        arg = sys.argv[i]
        for j in range(0, len(arg)):
            expression.append(arg[j])


    expression = construct_numbers_from_string_of_integers(expression)
    if (expression == None):
        return print("INVALID NUMBER FORMAT")


    # calculate decimal numbers
    expression = construct_decimal_numbers(expression)
    if (expression == None):
        return print("INVALID DECIMAL NUMBER FORMAT")


    # replace all PI symbols with value
    for i in range(0, len(expression)):
        if expression[i] in ['π', 'PI', 'pi', 'p']:
            expression[i] = math.pi

 
    # convert negative numbers
    expression = convert_negative_numbers(expression)
    if (expression == None):
        return print("INVALID NEGATIVE NUMBER FORMAT")
    
    
    # Calculate inner bracket expressions
    while expression.count('('):
        expression = calculate_innermost_brackets(expression, calculate_math)
        if (expression == None):
            return print("MATH ERROR")
    
    # Calculate final expression
    value = calculate_math(expression)
    if (value == None):
        return print("MATH ERROR")
      
    print(value)

if __name__ == "__main__":
    main()
