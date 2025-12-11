#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>

#define MAX_EXPR_LEN 1024

typedef struct {
    char expr[MAX_EXPR_LEN];
    int pos;
} Parser;

double factorial(double n) {
    if (n < 0) {
        fprintf(stderr, "Error: Factorial not defined for negative numbers\n");
        exit(1);
    }
    if (n == 0 || n == 1) {
        return 1.0;
    }
    
    double result = 1.0;
    for (int i = 2; i <= (int)n; i++) {
        result *= i;
    }
    return result;
}

void replaceSubstring(char *str, const char *old, const char *new) {
    char buffer[MAX_EXPR_LEN];
    char *pos;
    int oldLen = strlen(old);
    int newLen = strlen(new);
    
    while ((pos = strstr(str, old)) != NULL) {
        strcpy(buffer, pos + oldLen);
        strcpy(pos, new);
        strcpy(pos + newLen, buffer);
    }
}

void removeSpaces(char *str) {
    int i = 0, j = 0;
    while (str[i]) {
        if (!isspace(str[i])) {
            str[j++] = str[i];
        }
        i++;
    }
    str[j] = '\0';
}

void processFactorials(char *expr) {
    char temp[MAX_EXPR_LEN];
    char result[MAX_EXPR_LEN] = "";
    int i = 0, j = 0;
    
    while (expr[i]) {
        if (expr[i] == '!') {
            // Find the start of the number before '!'
            int start = j - 1;
            while (start >= 0 && (isdigit(temp[start]) || temp[start] == '.')) {
                start--;
            }
            start++;
            
            // Extract the number
            char numStr[64];
            int numLen = j - start;
            strncpy(numStr, temp + start, numLen);
            numStr[numLen] = '\0';
            
            double num = atof(numStr);
            double fact = factorial(num);
            
            // Replace with factorial result
            sprintf(temp + start, "%f", fact);
            j = start + strlen(temp + start);
            i++;
        } else {
            temp[j++] = expr[i++];
        }
    }
    temp[j] = '\0';
    strcpy(expr, temp);
}

void processFunctions(char *expr) {
    char temp[MAX_EXPR_LEN];
    int i = 0, j = 0;
    
    while (expr[i]) {
        if (isalpha(expr[i])) {
            char func = expr[i];
            i++;
            
            // Extract the number after the function
            char numStr[64];
            int k = 0;
            while (expr[i] && (isdigit(expr[i]) || expr[i] == '.')) {
                numStr[k++] = expr[i++];
            }
            numStr[k] = '\0';
            
            if (k > 0) {
                double num = atof(numStr);
                double result;
                
                switch (func) {
                    case 'S': result = sin(num); break;
                    case 's': result = sinh(num); break;
                    case 'C': result = cos(num); break;
                    case 'c': result = cosh(num); break;
                    case 'T': result = tan(num); break;
                    case 't': result = tanh(num); break;
                    case 'l': result = log(num); break;
                    case 'L': result = log10(num); break;
                    default:
                        temp[j++] = func;
                        strcpy(temp + j, numStr);
                        j += strlen(numStr);
                        continue;
                }
                
                sprintf(temp + j, "%f", result);
                j += strlen(temp + j);
            } else {
                temp[j++] = func;
            }
        } else {
            temp[j++] = expr[i++];
        }
    }
    temp[j] = '\0';
    strcpy(expr, temp);
}

void processPowerAndRoot(char *expr) {
    char temp[MAX_EXPR_LEN];
    int i = 0, j = 0;
    
    while (expr[i]) {
        if ((expr[i] == '^' || expr[i] == 'r') && j > 0) {
            char op = expr[i];
            
            // Find the first number (before operator)
            int start1 = j - 1;
            while (start1 >= 0 && (isdigit(temp[start1]) || temp[start1] == '.' || temp[start1] == '-')) {
                start1--;
            }
            start1++;
            
            char num1Str[64];
            int len1 = j - start1;
            strncpy(num1Str, temp + start1, len1);
            num1Str[len1] = '\0';
            
            // Extract the second number (after operator)
            i++;
            char num2Str[64];
            int k = 0;
            while (expr[i] && (isdigit(expr[i]) || expr[i] == '.')) {
                num2Str[k++] = expr[i++];
            }
            num2Str[k] = '\0';
            
            if (k > 0) {
                double a = atof(num1Str);
                double b = atof(num2Str);
                double result;
                
                if (op == '^') {
                    result = pow(a, b);
                } else { // 'r'
                    result = pow(a, 1.0 / b);
                }
                
                sprintf(temp + start1, "%f", result);
                j = start1 + strlen(temp + start1);
            } else {
                temp[j++] = op;
            }
        } else {
            temp[j++] = expr[i++];
        }
    }
    temp[j] = '\0';
    strcpy(expr, temp);
}

double parseNumber(Parser *p);
double parseFactor(Parser *p);
double parseTerm(Parser *p);
double parseExpression(Parser *p);

char currentChar(Parser *p) {
    if (p->pos < strlen(p->expr)) {
        return p->expr[p->pos];
    }
    return '\0';
}

void advance(Parser *p) {
    p->pos++;
}

void skipWhitespace(Parser *p) {
    while (currentChar(p) == ' ' || currentChar(p) == '\t') {
        advance(p);
    }
}

double parseNumber(Parser *p) {
    skipWhitespace(p);
    int start = p->pos;
    
    if (currentChar(p) == '-') {
        advance(p);
    }
    
    while (isdigit(currentChar(p)) || currentChar(p) == '.') {
        advance(p);
    }
    
    if (start == p->pos) {
        fprintf(stderr, "Error: Expected number at position %d\n", p->pos);
        exit(1);
    }
    
    char numStr[64];
    int len = p->pos - start;
    strncpy(numStr, p->expr + start, len);
    numStr[len] = '\0';
    
    return atof(numStr);
}

double parseFactor(Parser *p) {
    skipWhitespace(p);
    
    if (currentChar(p) == '-') {
        advance(p);
        return -parseFactor(p);
    }
    
    if (currentChar(p) == '+') {
        advance(p);
        return parseFactor(p);
    }
    
    if (currentChar(p) == '(') {
        advance(p);
        double result = parseExpression(p);
        skipWhitespace(p);
        if (currentChar(p) == ')') {
            advance(p);
        } else {
            fprintf(stderr, "Error: Expected ')'\n");
            exit(1);
        }
        return result;
    }
    
    return parseNumber(p);
}

double parseTerm(Parser *p) {
    double result = parseFactor(p);
    
    while (1) {
        skipWhitespace(p);
        char ch = currentChar(p);
        
        if (ch == '*') {
            advance(p);
            result *= parseFactor(p);
        } else if (ch == '/') {
            advance(p);
            result /= parseFactor(p);
        } else {
            break;
        }
    }
    
    return result;
}

double parseExpression(Parser *p) {
    double result = parseTerm(p);
    
    while (1) {
        skipWhitespace(p);
        char ch = currentChar(p);
        
        if (ch == '+') {
            advance(p);
            result += parseTerm(p);
        } else if (ch == '-') {
            advance(p);
            result -= parseTerm(p);
        } else {
            break;
        }
    }
    
    return result;
}

double evaluateExpression(char *input) {
    char expr[MAX_EXPR_LEN];
    strcpy(expr, input);
    
    removeSpaces(expr);
    
    // Replace p with pi
    char piStr[32];
    sprintf(piStr, "%f", M_PI);
    replaceSubstring(expr, "p", piStr);
    
    // Process in order: factorials, functions, power/root
    processFactorials(expr);
    processPowerAndRoot(expr);
    processFunctions(expr);
    
    // Parse the final expression
    Parser parser;
    strcpy(parser.expr, expr);
    parser.pos = 0;
    
    return parseExpression(&parser);
}

int main() {
    char input[MAX_EXPR_LEN];
    
    printf("==================================================\n");
    printf("CLI Calculator\n");
    printf("==================================================\n");
    printf("\nOperators:\n");
    printf("  S - Sin      s - SinH\n");
    printf("  C - Cos      c - CosH\n");
    printf("  T - Tan      t - TanH\n");
    printf("  ^ - Power    r - Root (arb = b-th root of a)\n");
    printf("  l - Ln       L - Log (base 10)\n");
    printf("  ! - Factorial\n");
    printf("  p - Pi (3.14159...)\n");
    printf("\nExamples:\n");
    printf("  1+2\n");
    printf("  2+5^4\n");
    printf("  2*(2+25)\n");
    printf("  6!\n");
    printf("  2+4^2\n");
    printf("  8r3 (cube root of 8)\n");
    printf("  L100 (log base 10 of 100)\n");
    printf("  S1.57 (sin of 1.57)\n");
    printf("\nType 'quit' or 'exit' to exit\n\n");
    printf("==================================================\n");
    
    while (1) {
        printf("\nEnter expression: ");
        
        if (fgets(input, sizeof(input), stdin) == NULL) {
            break;
        }
        
        // Remove newline
        input[strcspn(input, "\n")] = '\0';
        
        // Trim whitespace
        char *trimmed = input;
        while (isspace(*trimmed)) trimmed++;
        
        // Check for exit commands
        if (strcmp(trimmed, "quit") == 0 || 
            strcmp(trimmed, "exit") == 0 || 
            strcmp(trimmed, "q") == 0) {
            printf("Goodbye!\n");
            break;
        }
        
        // Skip empty input
        if (strlen(trimmed) == 0) {
            continue;
        }
        
        double result = evaluateExpression(trimmed);
        printf("Result: %f\n", result);
    }
    
    return 0;
}