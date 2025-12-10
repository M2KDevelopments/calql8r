#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h> 

// define boolean
#define TRUE  1
#define FALSE 0
#define FUNCTION_ERROR -1
#define FUNCTION_OK -2


// Any random characters that are not being used for anything
#define NUMBER 'N'
#define NUMBER_REMOVE 'M'
#define DECIMAL_POINT '.'

#define OPERATOR_ADD '+'
#define OPERATOR_SUBSTRACT '-'
#define OPERATOR_MULTPILY '*'
#define OPERATOR_DIVIDE '/'
#define OPERATOR_LOGx 'l'
#define OPERATOR_POW '^'
#define OPERATOR_ROOT 'r'
#define OPERATOR_SIN 'S'
#define OPERATOR_SINH 's'
#define OPERATOR_COS 'C'
#define OPERATOR_COSH 'c'
#define OPERATOR_TAN 'T'
#define OPERATOR_TANH 't'
#define OPERATOR_LOG10 'L'
#define OPERATOR_LN 'E'
#define OPERATOR_FACTORIAL '!'
#define PI 'p'
#define BRACKET_OPEN '('
#define BRACKET_CLOSE ')'
#define FACTORIAL '!'
#define PERMUTATIONS 'Y'
#define COMBINATIONS 'Z'

#define FUNCTION_VALUE_DIRECTION_RIGHT 1
#define FUNCTION_VALUE_DIRECTION_LEFT -1

#define CAL_ELEMENT_ERROR '_'
#define CAL_OK 0
#define CAL_ERROR_SYNTAX -1
#define CAL_ERROR_MATH -2

#define ARRAY_MAX_SIZE 80


struct Element{
    double value;
    int integers;
    char type;
    unsigned short digit_length;
};

struct Expression{
    struct Element elements[ARRAY_MAX_SIZE];
    unsigned short array_length;
    unsigned short error;
};

// https://www.youtube.com/watch?v=LscgaBzlGdE
char* trim_whitespaces(const char* text){
    
    // count how many no whitespace elements are there
    const char whitespace = ' ';
    int count = 0;
    int len = strlen(text);
    for(int i = 0; i < len; i++){
        char c = *(text + i);
        if(c != whitespace) count++;
    }

    /* n is the length of the array */
    char *list = (char *) malloc(sizeof (char) * ((count) +1)); 

    int j = 0;
    for(int i = 0; i < len; i++) {
        char c = *(text + i);

        // assign value to point
        if(c != whitespace) *(list + j++) = *(text + i);
        
    }

    // string terminator
    *(list + j) ='\0';
    
    return list;
}



struct Expression construct_numbers_from_string_of_integers(char* trimmed_expression){
    const int len = strlen(trimmed_expression);
    int start = -1;
    int end = 0;
    struct Expression expr;
    expr.error = CAL_OK;
    expr.array_length = 0;
    int expr_index = 0;

    for(int i = 0; i < len; i++){
        const char c = *(trimmed_expression + i);
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                if(start == -1) start = i;
                end = i;
                // last value of the list
                if(end == (len -1)){
                    int value = 0;
                    if(start != -1){
                        for(int j = end; j >= start; j--) {
                            int index = end - j;
                            int tenth = pow(10, index);
                            char num = *(trimmed_expression + j);
                            value += atoi(&num) * tenth;
                        }
                        
                        //add element to expression array
                        struct Element ele;
                        ele.integers = value;
                        ele.value = (double)value;
                        ele.type = NUMBER;
                        ele.digit_length = end - start +1;
                        expr.elements[expr_index++] = ele;

                        //reset counter
                        start = -1;
                        
                    }
                }
                break;
            default:
                int value = 0;
                if(start != -1){
                    for(int j = end; j >= start; j--) {
                        int index = end - j;
                        int tenth = pow(10, index);
                        char num = *(trimmed_expression + j);
                        value += atoi(&num) * tenth;
                    }
                    

                    //add element to expression array
                    struct Element ele;
                    ele.integers = value;
                    ele.value = (double)value;
                    ele.type = NUMBER;
                    ele.digit_length = end - start +1;
                    expr.elements[expr_index++] = ele;

                    // reset count
                    start = -1;
                }
                
                //add non-numeric elements
                //add element to expression array
                struct Element operatorElement;
                operatorElement.integers = 0;
                operatorElement.value = 0.0;
                operatorElement.type = c;
                expr.elements[expr_index++] = operatorElement;

                break;
        }
    }
    
    expr.array_length = expr_index;
    return expr;
}


struct Expression construct_decimal_numbers(struct Expression  expression){
    struct Expression expr;
    expr.array_length = 0;
    expr.error = CAL_OK;

    for(int i = 0; i < expression.array_length; i++){
        char c = expression.elements[i].type;
        if(c == DECIMAL_POINT){
            // if the decimal is on the first or last position
            if((i == 0) || (i == expression.array_length-1)){
                expr.error = CAL_ERROR_SYNTAX;
                return expr;
            }
            
            // Check if both side of the decimal are number values
            struct Element prev = expression.elements[i-1];
            struct Element  next = expression.elements[i+1];
            if(prev.type == NUMBER && next.type == NUMBER){
                float tenth = pow(10 , next.digit_length);
                expression.elements[i-1].value += next.value / tenth;
                expression.elements[i].type = NUMBER_REMOVE;
                expression.elements[i+1].type = NUMBER_REMOVE;
            }else{
                expr.error = CAL_ERROR_SYNTAX;
                return expr;
            }
        }
    }

    for(int i = 0; i < expression.array_length; i++){
        struct Element ele = expression.elements[i];
        if(ele.type != NUMBER_REMOVE) expr.elements[expr.array_length++] = ele;
    }
    return expr;
}

struct Expression convert_PI_values(struct Expression expression){
    for(int i =0; i< expression.array_length; i++){
        if(expression.elements[i].type == PI){
            expression.elements[i].type = NUMBER;
            expression.elements[i].integers = 3;
            expression.elements[i].value = M_PI; // M_PI is from <math.h> e.g 4.0 * atan(1.0);
            expression.elements->digit_length = 1;
        }
    }
    return expression;
}

struct Expression convert_negative_numbers(struct Expression expression){
    struct Expression expr;
    expr.array_length = 0;
    expr.error = CAL_OK;
 
    // e.g -1+2, 2*(-1), -2*-1/2--1*(-3-2) (1-2)-13
    for(int i = 0; i < expression.array_length; i++){
        char c = expression.elements[i].type;
        if(c == '-'){ //MINUS
            // if minus at the end of a expression
            if(i == expression.array_length-1){
                expr.error = CAL_ERROR_SYNTAX;
                return expr;
            }

            char next = expression.elements[i+1].type;
            if (i == 0 && next == NUMBER){
                expression.elements[i].type = NUMBER_REMOVE;
                expression.elements[i+1].value *-1;
                expression.elements[i+1].integers *-1;
            } else if(next == '-'){ // if minus is next to another minus e.g 1 -- 1 --> 1+1
                expression.elements[i].type = '+';
                expression.elements[i+1].type = NUMBER_REMOVE;
            } else if(i > 0 && expression.elements[i-1].type == BRACKET_OPEN && next == NUMBER){
                expression.elements[i].type = NUMBER_REMOVE;
                expression.elements[i+1].value *-1;
                expression.elements[i+1].integers *-1;
            } 
        }
    }

    for(int i = 0; i < expression.array_length; i++){
        struct Element ele = expression.elements[i];
        if(ele.type != NUMBER_REMOVE) expr.elements[expr.array_length++] = ele;
    }
    return expr;
}

struct Expression calculate_2_value_expressions(struct Expression expression, char FUNCTION_SYMBOL, struct Element (*callbackFunction) (double, double)){
    struct Expression expr;
    expr.array_length = 0;
    expr.error = CAL_OK;

    int prev_number_index = -1;
    for(int i = 0; i < expression.array_length; i++){
        char c = expression.elements[i].type;

        if(c == FUNCTION_SYMBOL){
            // if the decimal is on the first or last position
            if((i == 0) || (i == expression.array_length-1)){
                expr.error = CAL_ERROR_SYNTAX;
                return expr;
            }
            
            // Check if both side of the decimal are number values
            struct Element prev = expression.elements[i-1];
            struct Element next = expression.elements[i+1];
            if (prev.type == NUMBER && next.type == NUMBER && prev_number_index==-1){ 
                struct Element ele = callbackFunction(prev.value, next.value);
                if(ele.type == CAL_ELEMENT_ERROR){
                    expr.error = CAL_ERROR_SYNTAX;
                    return expr;
                }
                expression.elements[i-1] = ele;
                prev_number_index = i-1;
                expression.elements[i].type = NUMBER_REMOVE;
                expression.elements[i+1].type = NUMBER_REMOVE;
            }else if(next.type == NUMBER && prev_number_index > -1){  // There is a trailing NUMBER_REMOVE values
                prev = expression.elements[prev_number_index];
                struct Element ele = callbackFunction(prev.value, next.value);
                if(ele.type == CAL_ELEMENT_ERROR){
                    expr.error = CAL_ERROR_SYNTAX;
                    return expr;
                }
                expression.elements[prev_number_index] = ele;
                expression.elements[i].type = NUMBER_REMOVE;
                expression.elements[i+1].type = NUMBER_REMOVE;
            } else {
                expr.error = CAL_ERROR_SYNTAX;
                return expr;
            }
        }
        else if(c != NUMBER_REMOVE) prev_number_index =-1; // There isn't any trailing NUMBER_REMOVE values
    }

    for(int i = 0; i < expression.array_length; i++){
        struct Element ele = expression.elements[i];
        if(ele.type != NUMBER_REMOVE) expr.elements[expr.array_length++] = ele;
    }

    return expr;
}


struct Expression calculate_1_value_expression(struct Expression expression, char FUNCTION_SYMBOL, int function_value_direction, struct Element (*callbackFunction) (double)){
    struct Expression expr;
    expr.array_length = 0;
    expr.error = CAL_OK;

    for(int i = 0; i < expression.array_length; i++){
        char c = expression.elements[i].type;
        if(c == FUNCTION_SYMBOL){
            // if the decimal is on the first or last position
            if((i == 0 && function_value_direction == FUNCTION_VALUE_DIRECTION_LEFT) || ((i == (expression.array_length-1)) && function_value_direction == FUNCTION_VALUE_DIRECTION_RIGHT)){
                expr.error = CAL_ERROR_SYNTAX;
                return expr;
            }            

            if (function_value_direction == FUNCTION_VALUE_DIRECTION_LEFT){
                struct Element prev = expression.elements[i-1];
                if (prev.type != NUMBER){
                    expr.error = CAL_ERROR_SYNTAX;
                    return expr;
                }

                // calculate the value
                struct Element ele = callbackFunction(prev.value);

                if(ele.type == CAL_ELEMENT_ERROR){
                    expr.error = CAL_ERROR_SYNTAX;
                    return expr;
                }
                expression.elements[i-1] = ele;
                expression.elements[i].type = NUMBER_REMOVE;
            } else if (function_value_direction == FUNCTION_VALUE_DIRECTION_RIGHT){
                struct Element next = expression.elements[i+1];
                if (next.type != NUMBER){
                    expr.error = CAL_ERROR_SYNTAX;
                    return expr;
                }

                // calculate the value 
                struct Element ele = callbackFunction(next.value);


                if(ele.type == CAL_ELEMENT_ERROR){
                    expr.error = CAL_ERROR_SYNTAX;
                    return expr;
                }
                expression.elements[i].type = NUMBER_REMOVE;
                expression.elements[i+1] = ele;
            }
        }
    }

    for(int i = 0; i < expression.array_length; i++){
        struct Element ele = expression.elements[i];
        if(ele.type != NUMBER_REMOVE) expr.elements[expr.array_length++] = ele;
    }

    return expr;
}


struct Element calculate_sin(double num){
    struct Element e;
    e.integers = sin(num);
    e.value = sin(num);
    e.type = NUMBER;
    return e;
}

struct Element calculate_sinh(double num){
    struct Element e;
    e.integers = sinh(num);
    e.value = sinh(num);
    e.type = NUMBER;
    return e;
}

struct Element calculate_cos(double num){
    struct Element e;
    e.integers = cos(num);
    e.value = cos(num);
    e.type = NUMBER;
    return e;
}

struct Element calculate_cosh(double num){
    struct Element e;
    e.integers = cosh(num);
    e.value = cosh(num);
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_tan(double num){
    struct Element e;
    e.integers = tan(num);
    e.value = tan(num);
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_tanh(double num){
    struct Element e;
    e.integers = tanh(num);
    e.value = tanh(num);
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_log10(double num){
    struct Element e;
    if(num <= 0) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    e.integers = log10(num);
    e.value = log10(num);
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_ln(double num){
    struct Element e;
    if(num <= 0) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    e.integers = log(num)/log(exp(1));
    e.value = log(num)/log(exp(1));
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_e(double num){
    struct Element e;
    e.integers = exp(num);
    e.value = exp(num);
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_add(double num1, double num2){
    struct Element e;
    e.integers = num1 + num2;
    e.value = num1 + num2;
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_substract(double num1, double num2){
    struct Element e;
    e.integers = num1 - num2;
    e.value = num1 - num2;
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_multiply(double num1, double num2){
    struct Element e;
    e.integers = num1 * num2;
    e.value = num1 * num2;
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_divide(double num1, double num2){
    struct Element e;
    if(num2 ==0) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    e.integers = num1 / num2;
    e.value = num1 / num2;
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_pow(double base, double exp){
    struct Element e;
    e.integers = pow(base, exp);
    e.value = pow(base, exp);
    e.type = NUMBER;
    return e;
}

struct Element calculate_root(double num1, double num2){
    struct Element e;
    if(num1 <= 0) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    e.integers = pow(num2 , 1 / num1);
    e.value = pow(num2 , 1 / num1);
    e.type = NUMBER;
    return e;
    
}

struct Element calculate_log(double base, double raised){
    struct Element e;
    if(base <= 0 || raised <=0 ) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    e.integers = log(raised)/log(base);
    e.value = log(raised)/log(base);
    e.type = NUMBER;
    return e;
    
}

int resolve_factorial(int num){
    if(num < 0) return 0;
    if(num == 0) return 1;
    int total = 1;
    for(int n = 1; n <= num; n++) total*=n;
    return total;
}

struct Element calculate_permutation(double n1, double r1){
    int n = (int)n1;
    int r = (int) r1;
    struct Element e;
    if(n <= r || n < 0 || r < 0) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    double value = resolve_factorial(n)/resolve_factorial(n-r);
    e.integers = value;
    e.value = value;
    e.type = NUMBER;
    return e;
}


struct Element calculate_combinations(double n1, double r1){
    int n = (int)n1;
    int r = (int) r1;
    struct Element e;
    if(n < r || n < 0 || r < 0) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    double value =  resolve_factorial(n)/(resolve_factorial(r) * resolve_factorial(n-r));
    e.integers = value;
    e.value = value;
    e.type = NUMBER;
    return e;
}

struct Element calculate_factorial(double num){
    struct Element e;
    if(num < 0) {
        e.type = CAL_ELEMENT_ERROR;
        e.integers =0;
        e.value =0;
        return e;
    }
    double value =  resolve_factorial(num);
    e.integers = (int) value;
    e.value = value;
    e.type = NUMBER;
    return e;
}


struct Expression calculate_math(struct Expression expression){
    struct Expression expr;
    expr.array_length = 0;
    expr.error = CAL_OK; 

    // factorial and nPr and nCr 
    expr = calculate_1_value_expression(expression, OPERATOR_FACTORIAL, FUNCTION_VALUE_DIRECTION_LEFT, calculate_factorial);
    if(expr.error != CAL_OK) return expr;
 
    expr = calculate_2_value_expressions(expr, PERMUTATIONS, calculate_permutation);
    if(expr.error != CAL_OK) return expr;
    
    
    expr = calculate_2_value_expressions(expr, COMBINATIONS, calculate_combinations);
    if(expr.error != CAL_OK) return expr;

    // calculate trigonometry
    
    expr = calculate_1_value_expression(expr, OPERATOR_SIN, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_sin);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_1_value_expression(expr, OPERATOR_SINH, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_sinh);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_1_value_expression(expr, OPERATOR_COS, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_cos);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_1_value_expression(expr, OPERATOR_COSH, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_cosh);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_1_value_expression(expr, OPERATOR_TAN, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_tan);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_1_value_expression(expr, OPERATOR_TANH, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_tanh);
    if(expr.error != CAL_OK) return expr;

    
    // calculate logarithms
    expr = calculate_1_value_expression(expr, OPERATOR_LOG10, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_log10);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_1_value_expression(expr, OPERATOR_LN, FUNCTION_VALUE_DIRECTION_RIGHT, calculate_ln);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_2_value_expressions(expr, OPERATOR_LOGx, calculate_log);
    if(expr.error != CAL_OK) return expr;

    
    // calculate exponents and roots    
    expr = calculate_2_value_expressions(expr, OPERATOR_POW, calculate_pow);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_2_value_expressions(expr, OPERATOR_ROOT, calculate_root);
    if(expr.error != CAL_OK) return expr;
 
    // calculate basic arithmitic    
    expr = calculate_2_value_expressions(expr, OPERATOR_DIVIDE, calculate_divide);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_2_value_expressions(expr, OPERATOR_MULTPILY, calculate_multiply);
    if(expr.error != CAL_OK) return expr;
    
    expr = calculate_2_value_expressions(expr, OPERATOR_SUBSTRACT, calculate_substract);
    if(expr.error != CAL_OK) return expr;

    expr = calculate_2_value_expressions(expr, OPERATOR_ADD,  calculate_add);
    if(expr.error != CAL_OK) return expr;

    return expr;
}

struct Expression calculate_innermost_brackets(struct Expression expression){
    struct Expression expr;
    expr.array_length = 0;
    expr.error = CAL_OK;

    // check for brackets and get details
    // let first_open_bracket = -1;
    int last_open_bracket = -1;
    int first_close_bracket = -1;
    // let last_close_bracket = -1;
    int count_open_bracket = 0;
    int count_close_bracket = 0;


    for(int i = 0; i < expression.array_length; i++){
        // count the brackets
        if (expression.elements[i].type == BRACKET_OPEN) {
            last_open_bracket = i;
            count_open_bracket++;
        } else if (expression.elements[i].type == BRACKET_CLOSE) {
            count_close_bracket++;
            if (first_close_bracket == -1) first_close_bracket = i;
        }

        // if there are more close brackets than open ones mid-count error
        if (count_close_bracket > count_open_bracket) {
            expr.error = CAL_ERROR_SYNTAX;
            return expr;
        }

        // when the number of open brackets and closing brackets match.
        // 'last_open_bracket' is the start and 'first_close_bracket' is the end. for the calculation
        if (count_open_bracket == count_close_bracket && (first_close_bracket != -1)){

            //generate expression that is found in inner most brackets
            struct Expression bracket_expression;
            bracket_expression.error = CAL_OK;
            bracket_expression.array_length =0;
            for(int j = last_open_bracket + 1; j < first_close_bracket; j++){
                struct Element c = expression.elements[j];
                bracket_expression.elements[bracket_expression.array_length++] = c;
            }

            // calculate expression
            struct Expression calculated_expression = calculate_math(bracket_expression);

            // return error if any
            if(calculated_expression.error != CAL_OK) return expr;

            // calculation should only return an array of one element
            if(calculated_expression.array_length != 1){
                expr.error = CAL_ERROR_MATH;
                return expr;
            }

            // remove the bracket expression
            for(int j = 0; j < expression.array_length; j++){
                if(j == last_open_bracket){
                    expr.elements[expr.array_length++] = calculated_expression.elements[0];
                } else if (j < last_open_bracket || j > first_close_bracket){
                    expr.elements[expr.array_length++] = expression.elements[j];
                }  
            }

            // return calculated expression
            return expr;

        }

    }

    return expression;
}

int main(int argc, char *argv[]){
 
    if(argc <= 1){
        printf("PLEASE ADD AN EXPRESSION TO CALCULATE");
        return EXIT_FAILURE;
    }

    // example
    // char expression[] = "1465+225+55.7 36 63-9+8* 9 /8 + 2^2 + 2r4 + p + (1+1 + (2r4) + 3) + 6!+789";
    char expression[ARRAY_MAX_SIZE];
    int count = 0;
    for(int i = 1; i < argc; i++) {
        for(int j =0; j < strlen(argv[i]); j++) {
            expression[count++] = argv[i][j];
        }
    }
    
    // BUFFER SIZE CHECK
    int num_of_characters = strlen(expression);
    if(num_of_characters >= ARRAY_MAX_SIZE) {
        printf("%f is TOO MANY VALUES", num_of_characters);
        return EXIT_FAILURE;
    }

    char* trimmed_expression = trim_whitespaces(expression);
    struct Expression expr = construct_numbers_from_string_of_integers(trimmed_expression); 

    // free memory of trimmed expression
    free(trimmed_expression);

    // calculate decimal numbers
    expr = construct_decimal_numbers(expr); 

   

    // Check for Errors
    if(expr.error == CAL_ERROR_MATH) {
        printf("Math Error");
        return EXIT_FAILURE;
    }
    
    // Check for Errors
    if(expr.error == CAL_ERROR_SYNTAX) {
        printf("Syntax Error");
        return EXIT_FAILURE;
    }

    // replace all constants of pi
    expr = convert_PI_values(expr); 

   
    // Check for Errors
    if(expr.error == CAL_ERROR_MATH) {
        printf("Math Error");
        return EXIT_FAILURE;
    }
    
    // Check for Errors
    if(expr.error == CAL_ERROR_SYNTAX) {
        printf("Syntax Error");
        return EXIT_FAILURE;
    }

    // convert negative numbers and double negatives into positive
    expr = convert_negative_numbers(expr); 

    
    // Check for Errors
    if(expr.error == CAL_ERROR_MATH) {
        printf("Math Error");
        return EXIT_FAILURE;
    }
    
    // Check for Errors
    if(expr.error == CAL_ERROR_SYNTAX) {
        printf("Syntax Error");
        return EXIT_FAILURE;
    }

    
    // Calculate inner most bracket expression again and again
    int brackets_exists = 0;
    do{

        // calculations
        expr = calculate_innermost_brackets(expr);

        // Check for Errors
        if(expr.error == CAL_ERROR_MATH) {
            printf("Math Error");
            return EXIT_FAILURE;
        }
        
        // Check for Errors
        if(expr.error == CAL_ERROR_SYNTAX) {
            printf("Syntax Error");
            return EXIT_FAILURE;
        }

        // Check for brackets again
        brackets_exists = 0;
        for (int i = 0; i < expr.array_length; i++) {
            if(expr.elements[i].type == BRACKET_OPEN) {
                brackets_exists = 1; 
                break;
            }
        }

    } while(brackets_exists);

    // Check for Errors
    if(expr.error == CAL_ERROR_MATH) {
        printf("Math Error");
        return EXIT_FAILURE;
    }
    
    // Check for Errors
    if(expr.error == CAL_ERROR_SYNTAX) {
        printf("Syntax Error");
        return EXIT_FAILURE;
    }

    // final calculation
    expr = calculate_math(expr);

    // Check for Errors
    if(expr.error == CAL_ERROR_MATH) {
        printf("Math Error");
        return EXIT_FAILURE;
    }
    
    // Check for Errors
    if(expr.error == CAL_ERROR_SYNTAX) {
        printf("Syntax Error");
        return EXIT_FAILURE;
    }

    //show answer
    printf("\n\n");
    for(int i =0; i < expr.array_length; i++) {
        if(expr.elements[i].type == NUMBER) printf("%lf", expr.elements[i].value);
        else printf("%c", expr.elements[i].type);
    }
    return 0;
}