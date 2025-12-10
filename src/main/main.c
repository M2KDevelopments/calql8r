#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

#define PI_VALUE 3.14159265358979323846

// Forward declarations
double parse_expression(char **str);
double parse_term(char **str);
double parse_factor(char **str);
double parse_unary(char **str);
double parse_primary(char **str);

double factorial(double n) {
    if (n < 0) return 0;
    double r = 1;
    for (int i=1; i <= (int)n; i++) r *= i;
    return r;
}

void skip_spaces(char **str) {
    while (**str == ' ') (*str)++;
}

double parse_number(char **str) {
    double result = 0;
    double decimal = 0.1;

    if (**str == 'p') {       // constant π
        (*str)++;
        return PI_VALUE;
    }

    while (isdigit(**str)) {
        result = result * 10 + (**str - '0');
        (*str)++;
    }

    if (**str == '.') {
        (*str)++;
        while (isdigit(**str)) {
            result += (**str - '0') * decimal;
            decimal *= 0.1;
            (*str)++;
        }
    }
    return result;
}

// Parse functions like S() C() l() L() etc.
double parse_function(char **str) {
    char f = **str;
    (*str)++;

    double value = parse_primary(str);

    switch (f) {
        case 'S': return sin(value);
        case 's': return sinh(value);
        case 'C': return cos(value);
        case 'c': return cosh(value);
        case 'T': return tan(value);
        case 'h': return tanh(value);
        case 'l': return log(value);
        case 'L': return log10(value);
        case 'r': return pow(value, 0.5);  // default sqrt if r alone
    }

    return value;
}

double parse_primary(char **str) {
    skip_spaces(str);
    double value = 0;

    if (isalpha(**str)) { // function S,C,T,...
        return parse_function(str);
    }

    if (**str == '(') {
        (*str)++;
        value = parse_expression(str);
        if (**str == ')') (*str)++;
    } else {
        value = parse_number(str);
    }

    // factorial
    while (**str == '!') {
        (*str)++;
        value = factorial(value);
    }

    return value;
}

double parse_unary(char **str) {
    skip_spaces(str);

    if (**str == '+') {
        (*str)++;
        return parse_unary(str);
    }
    if (**str == '-') {
        (*str)++;
        return -parse_unary(str);
    }

    return parse_primary(str);
}

double parse_factor(char **str) {
    double base = parse_unary(str);

    while (**str == '^' || **str == 'r') {
        char op = **str;
        (*str)++;

        double exponent = parse_unary(str);

        if (op == '^') base = pow(base, exponent);
        else if (op == 'r') base = pow(exponent, 1.0 / base); // a r b = b^(1/a)
    }
    return base;
}

double parse_term(char **str) {
    double value = parse_factor(str);

    while (**str == '*' || **str == '/') {
        char op = **str;
        (*str)++;

        if (op == '*') value *= parse_factor(str);
        else value /= parse_factor(str);
    }
    return value;
}

double parse_expression(char **str) {
    double value = parse_term(str);

    while (**str == '+' || **str == '-') {
        char op = **str;
        (*str)++;

        if (op == '+') value += parse_term(str);
        else value -= parse_term(str);
    }
    return value;
}

int main() {
    char input[256];

    printf("CLI Scientific Calculator (type 'exit' to quit)\n");

    while (1) {
        printf(">");
        fgets(input, sizeof(input), stdin);

        if (strncmp(input, "exit", 4) == 0) break;

        char *p = input;
        double result = parse_expression(&p);

        printf("= %lf\n", result);
    }
    return 0;
}
