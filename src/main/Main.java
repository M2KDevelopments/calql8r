package main;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static char DECIMAL_POINT = '.';
    private static char OPERATOR_ADD = '+';
    private static char OPERATOR_SUBSTRACT = '-';
    private static char OPERATOR_MULTPILY = '*';
    private static char OPERATOR_DIVIDE = '/';
    private static char OPERATOR_LOGx = 'l';
    private static char OPERATOR_POW = '^';
    private static char OPERATOR_ROOT = 'r';
    private static char OPERATOR_SIN = 'S';
    private static char OPERATOR_SINH = 's';
    private static char OPERATOR_COS = 'C';
    private static char OPERATOR_COSH = 'c';
    private static char OPERATOR_TAN = 'T';
    private static char OPERATOR_TANH = 't';
    private static char OPERATOR_LOG10 = 'L';
    private static char OPERATOR_LN = 'E';
    private static char OPERATOR_FACTORIAL = '!';

    private static char FACTORIAL = '!';
    private static char PERMUTATIONS = 'Y';
    private static char COMBINATIONS = 'Z';

    private static enum EnumFunctionValueDirection {LEFT, RIGHT}

    private static class Element {
        public char type;
        public double value;
        public int integer;

        Element(char type, double value, int integer) {
            this.type = type;
            this.value = value;
            this.integer = integer;
        }
    }

    private static ArrayList construct_numbers_from_string_of_integers(ArrayList expression) {
        return expression;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("PLEASE ADD AN EXPRESSION TO CALCULATE");
            return;
        }

        var expression = new ArrayList();

        // construct expression for arguments e.g 1+1 +2 /4 *4
        // white spaces are automatically handled by joining each argument
        for (int i = 1; i < args.length; i++) {
            for (int j = 0; j < args[i].length(); j++) {
                expression.add(args[i].charAt(j));
            }
        }
        expression = construct_numbers_from_string_of_integers(expression);
        System.out.println(expression);
    }
} 