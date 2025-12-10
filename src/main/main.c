#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>

// --- Constants and Definitions ---
#define MAX_EXPR_LEN 256
#define MAX_TOKENS 100
#define MAX_STACK_SIZE 100
#define PI 3.14159265358979323846

// --- Utility Functions for Operator/Function Definitions ---

// Enum for Operator Associativity
typedef enum {
    LEFT_ASSOC,
    RIGHT_ASSOC
} Associativity;

// Enum for Token Types (for the parser)
typedef enum {
    TOKEN_NUMBER,
    TOKEN_OPERATOR,
    TOKEN_FUNCTION,
    TOKEN_LPAREN,
    TOKEN_RPAREN,
    TOKEN_EOF
} TokenType;

// Structure for Operator/Function Properties
typedef struct {
    char *name;         // Symbol or name (e.g., "+", "^", "S")
    int precedence;     // Precedence level (higher is tighter binding)
    Associativity assoc;
    int arity;          // 1 for unary, 2 for binary
    double (*func)(double, double); // Function pointer for evaluation
} OpFuncDef;

// --- Function Implementations ---

// Binary Operators
double op_add(double a, double b) { return a + b; }
double op_sub(double a, double b) { return a - b; }
double op_mul(double a, double b) { return a * b; }
double op_div(double a, double b) { return a / b; }
double op_pow(double a, double b) { return pow(a, b); }
// Custom Root: a r b means b-th root of a. (Root degree is b)
double op_root(double a, double b) { 
    if (a < 0 && fmod(b, 2.0) == 0.0) {
        fprintf(stderr, "Error: Cannot take even root of negative number.\n");
        return NAN;
    }
    return pow(a, 1.0 / b); 
}

// Unary Functions (Argument 'b' is unused, set to 0.0)
double func_sin(double a, double b) { return sin(a); }
double func_sinh(double a, double b) { return sinh(a); }
double func_cos(double a, double b) { return cos(a); }
double func_cosh(double a, double b) { return cosh(a); }
double func_tan(double a, double b) { return tan(a); }
double func_tanh(double a, double b) { return tanh(a); }
double func_ln(double a, double b) { return log(a); }
double func_log(double a, double b) { return log10(a); }

// Unary Post-fix Factorial
double func_factorial(double a, double b) { 
    if (a < 0 || fmod(a, 1.0) != 0.0) {
        fprintf(stderr, "Error: Factorial only defined for non-negative integers.\n");
        return NAN;
    }
    long long res = 1;
    for (int i = 1; i <= (int)a; i++) {
        res *= i;
    }
    return (double)res;
}

// --- Operator/Function Definition Array ---
OpFuncDef op_defs[] = {
    // Unary Postfix (Highest Precedence)
    {"!", 6, LEFT_ASSOC, 1, func_factorial}, 

    // Unary Prefix Functions (Second Highest)
    {"S", 5, RIGHT_ASSOC, 1, func_sin},
    {"s", 5, RIGHT_ASSOC, 1, func_sinh},
    {"C", 5, RIGHT_ASSOC, 1, func_cos},
    {"c", 5, RIGHT_ASSOC, 1, func_cosh},
    {"T", 5, RIGHT_ASSOC, 1, func_tan},
    {"t", 5, RIGHT_ASSOC, 1, func_tanh},
    {"l", 5, RIGHT_ASSOC, 1, func_ln}, // Ln
    {"L", 5, RIGHT_ASSOC, 1, func_log}, // Log10

    // Binary Operators (Higher Precedence)
    {"^", 4, RIGHT_ASSOC, 2, op_pow},
    {"r", 4, LEFT_ASSOC, 2, op_root}, // Root

    // Binary Operators (Lower Precedence)
    {"*", 3, LEFT_ASSOC, 2, op_mul},
    {"/", 3, LEFT_ASSOC, 2, op_div},
    {"+", 2, LEFT_ASSOC, 2, op_add},
    {"-", 2, LEFT_ASSOC, 2, op_sub}, // Note: This is BINARY subtraction

    {NULL, 0, LEFT_ASSOC, 0, NULL} // Sentinel
};

// Find the definition for an operator/function string
OpFuncDef *find_op_def(const char *op_str) {
    for (int i = 0; op_defs[i].name != NULL; i++) {
        if (strcmp(op_defs[i].name, op_str) == 0) {
            return &op_defs[i];
        }
    }
    return NULL;
}

// --- Stack Implementations (for Shunting-Yard and RPN Evaluation) ---

// Stack for Operators/Functions (Stores pointers to OpFuncDef or NULL for parentheses)
typedef struct {
    OpFuncDef *data[MAX_STACK_SIZE];
    int top;
} OpStack;

void push_op(OpStack *s, OpFuncDef *val) {
    if (s->top >= MAX_STACK_SIZE - 1) {
        fprintf(stderr, "Error: Operator stack overflow.\n");
        exit(EXIT_FAILURE);
    }
    s->data[++s->top] = val;
}

OpFuncDef *pop_op(OpStack *s) {
    if (s->top < 0) return NULL;
    return s->data[s->top--];
}

OpFuncDef *peek_op(OpStack *s) {
    if (s->top < 0) return NULL;
    return s->data[s->top];
}

// Stack for Values (Used during RPN evaluation)
typedef struct {
    double data[MAX_STACK_SIZE];
    int top;
} ValueStack;

void push_value(ValueStack *s, double val) {
    if (s->top >= MAX_STACK_SIZE - 1) {
        fprintf(stderr, "Error: Value stack overflow.\n");
        exit(EXIT_FAILURE);
    }
    s->data[++s->top] = val;
}

double pop_value(ValueStack *s) {
    if (s->top < 0) {
        fprintf(stderr, "Error: Value stack underflow (not enough operands).\n");
        exit(EXIT_FAILURE);
    }
    return s->data[s->top--];
}


// --- RPN Evaluation Logic ---

/**
 * Evaluates an expression in Reverse Polish Notation (RPN).
 * Tokens must be separated by spaces.
 */
double evaluate_rpn(char **tokens, int token_count) {
    ValueStack stack = {.top = -1};
    OpFuncDef *def;
    double a, b, result;

    for (int i = 0; i < token_count; i++) {
        char *token = tokens[i];

        // 1. Check for number/constant
        if (isdigit(token[0]) || (token[0] == '-' && isdigit(token[1])) || token[0] == '.') {
            push_value(&stack, atof(token));
        } else if (strcmp(token, "p") == 0) {
            push_value(&stack, PI);
        }
        // 2. Check for operator/function
        else if ((def = find_op_def(token)) != NULL) {
            if (def->arity == 2) { // Binary operator
                if (stack.top < 1) { fprintf(stderr, "Error: Binary op '%s' requires 2 operands.\n", token); return NAN; }
                b = pop_value(&stack);
                a = pop_value(&stack);
                result = def->func(a, b);
                push_value(&stack, result);
            } else if (def->arity == 1) { // Unary operator/function
                if (stack.top < 0) { fprintf(stderr, "Error: Unary op '%s' requires 1 operand.\n", token); return NAN; }
                a = pop_value(&stack);
                result = def->func(a, 0.0); // Pass 0.0 for the dummy second argument
                push_value(&stack, result);
            }
        } else {
            fprintf(stderr, "Error: Unknown token '%s' in RPN.\n", token);
            return NAN;
        }

        if (isnan(result) && (def != NULL)) { // Check for math domain errors
            return NAN; 
        }
    }

    if (stack.top == 0) {
        return pop_value(&stack);
    } else {
        fprintf(stderr, "Error: Invalid RPN expression (too many/few operands).\n");
        return NAN;
    }
}

// --- Shunting-Yard Algorithm (Infix to RPN) ---

/**
 * Simplified Tokenizer: Splits the input string into tokens (numbers,
 * operators, functions, parentheses, and constants).
 * IMPORTANT: Tokens must be separated by spaces in the input string.
 */
int tokenize(const char *infix_expr, char **tokens) {
    char *expr_copy = strdup(infix_expr);
    char *token = strtok(expr_copy, " ");
    int count = 0;
    while (token != NULL && count < MAX_TOKENS) {
        tokens[count++] = strdup(token);
        token = strtok(NULL, " ");
    }
    free(expr_copy);
    return count;
}

/**
 * Converts tokenized infix expression to RPN using Shunting-Yard.
 */
int infix_to_rpn(char **infix_tokens, int infix_count, char **rpn_tokens) {
    OpStack op_stack = {.top = -1};
    int rpn_count = 0;
    OpFuncDef *def;

    for (int i = 0; i < infix_count; i++) {
        char *token = infix_tokens[i];

        // 1. If it's a number or constant 'p'
        if (isdigit(token[0]) || (token[0] == '-' && isdigit(token[1])) || token[0] == '.' || strcmp(token, "p") == 0) {
            rpn_tokens[rpn_count++] = token; // Add to RPN output
        }
        // 2. If it's a function or '('
        else if (token[0] == '(' || (def = find_op_def(token)) != NULL && def->arity == 1 && strcmp(def->name, "!") != 0) {
            push_op(&op_stack, (token[0] == '(') ? NULL : def); // Push '(' (NULL) or Function
        }
        // 3. If it's an operator
        else if ((def = find_op_def(token)) != NULL && def->arity == 2) {
            while (op_stack.top >= 0) {
                OpFuncDef *top_op = peek_op(&op_stack);
                if (top_op == NULL) break; // Reached '('
                
                int top_prec = top_op->precedence;
                int curr_prec = def->precedence;

                if ((def->assoc == LEFT_ASSOC && curr_prec <= top_prec) ||
                    (def->assoc == RIGHT_ASSOC && curr_prec < top_prec)) {
                    rpn_tokens[rpn_count++] = pop_op(&op_stack)->name;
                } else {
                    break;
                }
            }
            push_op(&op_stack, def);
        }
        // 4. If it's a postfix operator '!'
        else if (strcmp(token, "!") == 0) {
             rpn_tokens[rpn_count++] = token; // Postfix operator immediately goes to output
        }
        // 5. If it's ')'
        else if (token[0] == ')') {
            while (op_stack.top >= 0 && peek_op(&op_stack) != NULL) { // Pop until '('
                rpn_tokens[rpn_count++] = pop_op(&op_stack)->name;
            }
            if (op_stack.top < 0) { fprintf(stderr, "Error: Mismatched parentheses.\n"); return -1; }
            pop_op(&op_stack); // Pop the '('
        } else {
            fprintf(stderr, "Error: Invalid token during parsing: %s\n", token);
            return -1;
        }
    }

    // 6. Pop remaining operators from stack to RPN output
    while (op_stack.top >= 0) {
        OpFuncDef *top_op = pop_op(&op_stack);
        if (top_op == NULL) {
            fprintf(stderr, "Error: Mismatched parentheses.\n"); return -1;
        }
        rpn_tokens[rpn_count++] = top_op->name;
    }

    return rpn_count;
}

// --- Main Calculation Logic ---

double calculate(const char *expression) {
    char *infix_tokens_heap[MAX_TOKENS]; // Tokens created by strdup in tokenize
    char *rpn_tokens_name[MAX_TOKENS];   // Pointers to names (literals/OpFuncDef names)

    // Step 1: Tokenize the input string (assumes space separation)
    int infix_count = tokenize(expression, infix_tokens_heap);
    if (infix_count == 0) return NAN;

    // Step 2: Convert Infix to RPN
    int rpn_count = infix_to_rpn(infix_tokens_heap, infix_count, rpn_tokens_name);
    if (rpn_count == -1) {
        for (int i = 0; i < infix_count; i++) free(infix_tokens_heap[i]);
        return NAN;
    }

    // DEBUG: Print RPN
    // printf("RPN: ");
    // for(int i=0; i < rpn_count; i++) printf("%s ", rpn_tokens_name[i]);
    // printf("\n");

    // Step 3: Evaluate the RPN expression
    double result = evaluate_rpn(rpn_tokens_name, rpn_count);

    // Clean up allocated memory from tokenization
    for (int i = 0; i < infix_count; i++) free(infix_tokens_heap[i]);
    
    return result;
}

// --- Main CLI Interface ---

int main() {
    char expression[MAX_EXPR_LEN];
    double result;

    printf("--- CLI Calculator (Infix Mode) ---\n");
    printf("--- NOTE: All tokens (numbers, operators, functions, parentheses) MUST be separated by a space. ---\n");
    printf("--- Example: 1 + 2 * ( C 45 ) + 2 r 4 + 6 ! ---\n");
    printf("Supported Operations:\n");
    printf("  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)\n");
    printf("  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S 30)\n");
    printf("  Unary (Postfix): ! (Factorial, e.g., 6 !)\n");
    printf("  Constant: p (PI)\n");
    printf("Type 'exit' or 'quit' to end.\n\n");

    while (1) {
        printf("Expression: ");
        if (fgets(expression, MAX_EXPR_LEN, stdin) == NULL) {
            break;
        }

        // Remove newline character
        expression[strcspn(expression, "\n")] = 0;

        // Check for exit commands
        if (strcmp(expression, "exit") == 0 || strcmp(expression, "quit") == 0 || strlen(expression) == 0) {
            break;
        }

        // Calculate the result
        result = calculate(expression);

        // Print the result
        if (!isnan(result)) {
            printf("Result: **%.10f**\n\n", result);
        } else {
            // Error messages are handled inside the calculate/evaluation functions
            printf("Calculation failed or resulted in an undefined value (e.g., log(-1)).\n\n");
        }
    }

    printf("Exiting calculator. Goodbye!\n");
    return 0;
}