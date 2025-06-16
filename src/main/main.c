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
#define NUMBER_STR 'Z'
#define NUMBER 'O'
#define NUMBER_REMOVE 'M'
#define DECIMAL_POINT '.'

#define OPERATOR_ADD '+'
#define OPERATOR_SUBSTRACT '-'
#define OPERATOR_MULTPILY '*'
#define OPERATOR_DIVIDE '/'
#define OPERATOR_LOGx 'l'
#define OPERATOR_POW '^'
#define OPERATOR_ROOT 'r'



struct Element{
    double value;
    int integers;
    char type;
};

struct ElementItems
{
    int size;
    struct Element* elements;
    const char *error;
};


struct ElementFuncProps{
    double num1;
    double num2;
    int symbol_index;
    const char *error;
};


// https://www.youtube.com/watch?v=LscgaBzlGdE
char* trim(const char* text){
    
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

struct Element* construct_elements_array(char * list){
    int len = strlen(list);
    struct Element* items = (struct Element*) malloc(sizeof(struct Element) * len);
    for(int i = 0; i < len; i++){

        // Assign struct to array
        char c = *(list + i);
        struct Element element;
        if(c == '1' || c=='2' || c=='3' || c=='4' || c == '5'|| c == '6'|| c == '7'|| c == '8'|| c == '9'|| c == '0'){
            int v =  atoi(&c);
            element.type = NUMBER_STR;
            element.value = (double) v;
            element.integers = v;
        }else{
            element.type = c;
            element.value = 0.0;
            element.integers = 0;
        }
        items[i] = element;
    }
   
    return items;
}

struct Element* construct_numbers_from_string_of_integers(struct Element* elements, int len){
    
    int string_numbers_exists = FALSE;
    int start = -1;
     
    do{
        for(int i =0; i < len; i++){
            struct Element ele = *(elements + i);
            if(ele.type == NUMBER_STR){
                if(start == -1) start = i;

                // number is at the end
                if((start != -1) && (i == (len -1))){
                    int end = i;
                    int value = 0;
                    int multipler = 1;
                    for(int j = end; j >= start; j--){
                        struct Element num = *(elements + j);
                        value += num.value * multipler;
                        multipler *= 10;

                        // remove NUMBER_STR elements
                        elements[j].type = NUMBER_REMOVE;
                    }

                    

                    // Number Element replace it to the first part of the NUMBER_STR
                    elements[start].value = value;
                    elements[start].integers = value;
                    elements[start].type = NUMBER; 
                }

            }else{

                // skip if a number has not been found yet
                if (start == -1) continue;

                // calculate the number
                int end = i;
                int value = 0;
                int multipler = 1;
                for (int j = end - 1; j >= start; j--) {
                    struct Element num = *(elements + j);
                    value += num.value * multipler;
                    multipler *= 10;

                    // remove NUMBER_STR elements
                    elements[j].type = NUMBER_REMOVE;
                }

                // Number Element replace it to the first part of the NUMBER_STR
                elements[start].value = value;
                elements[start].integers = value;
                elements[start].type = NUMBER; 

                // reset start position
                start = -1;
                break;
            }
        }

        // check if there are NUMBER_STR
        string_numbers_exists = FALSE;
        for(int i =0; i < len; i++){
            struct Element ele = *(elements + i);
            if(ele.type == NUMBER_STR){
                string_numbers_exists = TRUE;
                break;
            }
        }
    } while(string_numbers_exists == TRUE);
    return elements;
}

struct ElementItems remove_string_numbers(struct Element* elements, int len){
    
    // count number of NUMBER_REMOVE so that space can be defined
    int count = 0;
    for(int i =0; i < len; i++){
        struct Element ele = *(elements + i);
        if(ele.type != NUMBER_REMOVE) count++;
    }

    // Create new elements array remove all NUMBER_REMOVE
    struct Element* list = (struct Element*) malloc(sizeof(struct Element) * count);

    int j =0;
    for(int i = 0; i < len; i++){
        struct Element ele = *(elements + i);
        if(ele.type != NUMBER_REMOVE) list[j++] = ele;
    }

    // define element items so that I can pass the pointer to the array and size
    struct ElementItems data;
    data.size = count;
    data.elements = list;
    return data;
}

struct ElementItems construct_decimal_numbers(struct Element* elements, int len){
    int decimal_available = FALSE;
    int decimal_count = 0;
    
    do {

        for(int i = 0; i < len; i++){
            struct Element ele = *(elements + i);
            
            // find the decimal points
            if(ele.type == DECIMAL_POINT) {

                // check if decimal is in the wrong place
                if ((i - 1 < 0) || (i + 1 > len - 1)) {
                    struct ElementItems e;
                    e.size = 0;
                    e.error = "Math Error: Could not parse decimal";
                    return e;  
                }

                // Get left and right number
                struct Element ele_left = elements[i-1];
                struct Element ele_right = elements[i+1];

                // Check if either left or right number is not a valid number
                if(ele_left.type != NUMBER || ele_left.type != NUMBER) {
                    struct ElementItems e;
                    e.size = 0;
                    e.error = "Math Error: Could not parse decimal";
                    return e; 
                }

                // calculate decimal
                int n = ele_right.integers;
                int multiper = 1;

                // we've found how many times
                // we need to divid to get to n
                while (n != (n % multiper)) multiper *= 10;
                double decimal = ele_right.value / multiper;
                elements[i-1].value += decimal;
                

                // Set the Elements to be removed
                elements[i].type = NUMBER_REMOVE;
                elements[i+1].type = NUMBER_REMOVE;


                // increment decimal count
                decimal_count++;
            } 
        }


        // check for decimal points
        for(int i = 0; i < len; i++){
            struct Element ele = *(elements + i);
            if(ele.type == DECIMAL_POINT) {
                decimal_available == TRUE;
                break;
            }
        }

    } while (decimal_available == TRUE);


    // Remove decimal points and right number
    // #1.#2 remove . and #2 
    int size = len - (decimal_count * 2);
    struct Element* list = (struct Element*) malloc(sizeof(elements) * size);

    // Add elements to array
    int j =0;
    for(int i=0; i< len; i++){
        if(elements[i].type != NUMBER_REMOVE) list[j++] = elements[i];
    }

 
    // Construct new element list
    struct ElementItems items;
    items.elements = list;
    items.size = size;
    return items;

}

// check if symbol still exists
int symbol_exists(struct Element* elements, int length, char symbol){
    for(int i = 0 ; i < length; i++){
        struct Element ele = elements[i];
        if(ele.type == symbol) return TRUE;
    }
    return FALSE;
}

/**
 * Calculate expression which require 2 numbers e.g 5*5 or 5+2 or 3^4 
 * if left element and right are NOT both numbers it will just skip because it might be an expression. So that it can be calculate it later when the expression is calculate by other functions
 */
struct ElementFuncProps calculate_2_value_expressions_props(char function_symbol, struct Element* elements, int len, const char* error_message){
    
    for(int i = 0; i < len; i++){
            struct Element ele = *(elements + i);
            
            // find the decimal points
            if(ele.type == function_symbol) {

                // check if decimal is in the wrong place
                if ((i - 1 < 0) || (i + 1 > len - 1)) {
                    struct ElementFuncProps e;
                    e.symbol_index = FUNCTION_ERROR;
                    e.error = error_message;
                    return e;  
                }

                // Get left and right number
                struct Element ele_left = elements[i-1];
                struct Element ele_right = elements[i+1];

                // Check if either left or right number is not a valid number
                if(ele_left.type == NUMBER && ele_right.type == NUMBER) {
                    struct ElementFuncProps e;
                    e.symbol_index = i;
                    e.num1 = ele_left.value;
                    e.num2 = ele_right.value;
                  
                    return e; 
                }
                // if left element and right are NOT both numbers just skip because it might be an express
                // so we will calculate it later when the expression is calculate by other functions
            } 
    }
 
    struct ElementFuncProps ok;
    ok.symbol_index = FUNCTION_OK;
    return ok;  
}


struct ElementItems calculate_2_value_expressions(struct Element* list, int length, char symbol_char, int error_on_zero, int error_on_negative, double (*callbackFunction) (double, double)){
    
    // This item has an error
    if(!length) {
            // if left element and right are NOT both numbers just skip because it might be an express
            // so we will calculate it later when the expression is calculate by other functions
            struct ElementItems e;
            e.elements = list;
            e.size = length;
            return e; 
        } 
    
    // Get elements
    struct Element* elements = list;
    struct ElementItems data;
    data.size = 0;


    int len = length;
    int symbol_available = symbol_exists(list, length, symbol_char);
    

    while(symbol_available){
        struct ElementFuncProps props = calculate_2_value_expressions_props(symbol_char, elements, len, "Math Error");
        if (props.symbol_index == FUNCTION_ERROR){
            struct ElementItems e;
            e.size = 0;
            return e;
        } else if(props.symbol_index == FUNCTION_OK){
            // if left element and right are NOT both numbers just skip because it might be an express
            // so we will calculate it later when the expression is calculate by other functions
              struct ElementItems e;
                e.elements = list;
                e.size = length;
                return e;
        } 
        
        // Check for math error
        if (props.num2 == 0 && error_on_zero == TRUE){
            struct ElementItems e;
            e.size = 0;
            e.error = "Math Error: Dividing by 0";
            return e;
        }

        if(error_on_negative && props.num1 < 0){
            struct ElementItems e;
            e.size = 0;
            e.error = "Math Error: Calculation on negative number";
            return e;
        }
        
        // Math calculation here
        double value = callbackFunction(props.num1, props.num2);
       
        // Set to remove the other nubmers
        elements[props.symbol_index-1].value = value;
        elements[props.symbol_index].type = NUMBER_REMOVE;
        elements[props.symbol_index + 1].type = NUMBER_REMOVE;

        // reassign items - new memory was created here
        if(data.size) free(data.elements);
        struct ElementItems data = remove_string_numbers(elements, len);

        // free memory - from function parameter
        if(elements && !data.size) free(elements);

        // update element
        elements = data.elements;
        len = data.size;

        // re-check for symbol
        symbol_available = symbol_exists(elements, len, symbol_char);
    }

    // Return Elements after calculation
    struct ElementItems e;
    e.elements = elements;
    e.size = len;
    return e;
}


double calculate_add(double num1, double num2){
    return num1 + num2;
}

double calculate_substract(double num1, double num2){
    return num1 - num2;
}

double calculate_multiply(double num1, double num2){
    return num1 * num2;
}

double calculate_divide(double num1, double num2){
    return num1 / num2;
}

double calculate_pow(double base, double exp){
    return pow(base, exp);
}

double calculate_root(double num1, double num2){
    return pow(num2 , 1 / num1);
}

double calculate_log(double base, double raised){
    return log(raised)/log(base);
}

void printexpression(struct Element* list, int len){

    printf("\n-----------------------------\n\n" );
    for(int i = 0; i < len; i++) {
        struct Element num = *(list + i);
        if(num.type == NUMBER_REMOVE) continue;//skip
        if(num.type == NUMBER) printf("%f", num.value);
        else printf("%c", num.type);
    }
    printf("\n-----------------------------\n\n" );
    
}

int main(){
   
    const char expression[] = "1+225+55.7 36 63-9+8* 9 /8 + 2^2 + 2r4 ";
    char* elements = trim(expression);
    int len = strlen(elements);
    

    // Get Elements of the expression
    struct Element* list = construct_elements_array(elements);
    list = construct_numbers_from_string_of_integers(list, len);  
    struct ElementItems data = remove_string_numbers(list, len);

    list = data.elements;
    data = construct_decimal_numbers(data.elements, data.size);

    printexpression(data.elements, data.size);
    if(list) free(list); // free up memory

    // free up memory - these functions
    list = data.elements;
    data = calculate_2_value_expressions(data.elements, data.size, OPERATOR_POW, FALSE, FALSE, calculate_pow);
    data = calculate_2_value_expressions(data.elements, data.size, OPERATOR_ROOT, FALSE, TRUE, calculate_root);
    data = calculate_2_value_expressions(data.elements, data.size, OPERATOR_LOGx, FALSE, FALSE, calculate_log);
    data = calculate_2_value_expressions(data.elements, data.size, OPERATOR_DIVIDE, TRUE, FALSE,calculate_divide);
    data = calculate_2_value_expressions(data.elements, data.size, OPERATOR_MULTPILY, FALSE, FALSE, calculate_multiply);
    data = calculate_2_value_expressions(data.elements, data.size, OPERATOR_SUBSTRACT, FALSE, FALSE, calculate_substract);
    data = calculate_2_value_expressions(data.elements, data.size, OPERATOR_ADD, FALSE, FALSE, calculate_add);
    printexpression(data.elements, data.size);
  
    
    // free up memory
    if(elements) free(elements);

    return 0;
} 
