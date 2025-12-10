import math
import re
import sys

# --- 1. Operator and Function Definitions ---

class OpFuncDef:
    """Represents an operator or function with its properties."""
    def __init__(self, name, precedence, is_left_associative, arity, func):
        self.name = name
        self.precedence = precedence
        self.is_left_associative = is_left_associative
        self.arity = arity  # 1 for unary, 2 for binary
        self.func = func    # The actual Python function

# Global static map for all known operators and functions
DEFINITIONS = {}

# --- 2. Function Implementations ---

# Custom Root: a r b means b-th root of a. (Root degree is b)
def _root(a, b):
    if a < 0 and b % 2.0 == 0.0:
        raise ValueError("Cannot take even root of a negative number.")
    return a ** (1.0 / b)

# Unary Post-fix Factorial
def _factorial(a):
    if a < 0 or a % 1.0 != 0.0:
        raise ValueError("Factorial only defined for non-negative integers.")
    return math.factorial(int(a))

# --- 3. Static Initialization ---

def initialize_definitions():
    """Initializes the static function definitions map."""
    global DEFINITIONS
    if DEFINITIONS:
        return

    # Precedence: Higher number binds tighter
    
    # Postfix Unary (Highest Precedence)
    DEFINITIONS["!"] = OpFuncDef("!", 6, True, 1, _factorial) 

    # Prefix Unary Functions (Right Associative precedence 5)
    DEFINITIONS["S"] = OpFuncDef("S", 5, False, 1, math.sin)
    DEFINITIONS["s"] = OpFuncDef("s", 5, False, 1, math.sinh)
    DEFINITIONS["C"] = OpFuncDef("C", 5, False, 1, math.cos)
    DEFINITIONS["c"] = OpFuncDef("c", 5, False, 1, math.cosh)
    DEFINITIONS["T"] = OpFuncDef("T", 5, False, 1, math.tan)
    DEFINITIONS["t"] = OpFuncDef("t", 5, False, 1, math.tanh)
    DEFINITIONS["l"] = OpFuncDef("l", 5, False, 1, math.log)    # Ln (Natural Log)
    DEFINITIONS["L"] = OpFuncDef("L", 5, False, 1, math.log10) # Log10

    # Binary Operators
    DEFINITIONS["^"] = OpFuncDef("^", 4, False, 2, math.pow) # Right Associative
    DEFINITIONS["r"] = OpFuncDef("r", 4, True, 2, _root)    # Left Associative

    DEFINITIONS["*"] = OpFuncDef("*", 3, True, 2, lambda a, b: a * b)
    DEFINITIONS["/"] = OpFuncDef("/", 3, True, 2, lambda a, b: a / b)
    
    DEFINITIONS["+"] = OpFuncDef("+", 2, True, 2, lambda a, b: a + b)
    # Internal token '_' for binary subtraction, distinguishing it from unary minus.
    DEFINITIONS["_"] = OpFuncDef("-", 2, True, 2, lambda a, b: a - b) 

# --- 4. Lexer/Tokenizer ---

def _is_number_or_constant(s):
    """Checks if a string is a number or the constant 'p'."""
    if s == "p": return True
    try:
        float(s)
        return True
    except ValueError:
        return False

def _tokenize(expression):
    """Converts the raw input string into a list of tokens, handling unary minus."""
    expression = expression.replace(' ', '')
    
    # 1. Separate all single-character tokens (functions, operators, parentheses, constant 'p')
    # Use a regex to look for all these characters, inserting spaces around them.
    # Note: We must handle '-' separately to avoid incorrect separation of negative numbers.
    token_pattern = r'([SsCcTtLl\^\*\/\!\(\)r]|\d+\.?\d*|\.\d+|p)'
    
    # Insert spaces around all relevant symbols except the minus sign initially
    processed = expression
    for sep in list("()^*/!rSsCcTtLl"):
        processed = processed.replace(sep, f' {sep} ')
    processed = processed.replace("p", ' p ')
    
    # Clean up multiple spaces and split
    tokens = [t for t in processed.split() if t]

    # 2. Handle Unary Minus vs. Binary Minus
    result_tokens = []
    
    for i, token in enumerate(tokens):
        if token == "-":
            # Check if the preceding token is an operand (number, constant, '!', or ')')
            is_preceding_token_operand = result_tokens and \
                (_is_number_or_constant(result_tokens[-1]) or result_tokens[-1] in ("!", ")"))
            
            if i == 0 or not is_preceding_token_operand:
                # Unary Minus: Merge it with the next token (e.g., "-5")
                if i + 1 < len(tokens):
                    tokens[i + 1] = token + tokens[i + 1]
                # Skip the current token as it's merged into the next
            else:
                # Binary Minus: Use the internal token '_'
                result_tokens.append("_")
        elif token == "+":
            # Treat '+' as a binary operator always, unary '+' is redundant
            result_tokens.append("+")
        else:
            result_tokens.append(token)
            
    return result_tokens


# --- 5. Shunting-Yard Algorithm (Infix to RPN) ---

def _infix_to_rpn(infix_tokens):
    """Converts a list of infix tokens to a list of RPN tokens."""
    output_queue = []
    operator_stack = []
    
    for token in infix_tokens:
        # 1. Number or constant 'p'
        if _is_number_or_constant(token):
            output_queue.append(token)
        # 2. Function or prefix unary operator (not '!')
        elif token in DEFINITIONS and DEFINITIONS[token].arity == 1 and token != "!":
            operator_stack.append(token)
        # 3. Binary operator ('_' is binary subtraction)
        elif token in DEFINITIONS and DEFINITIONS[token].arity == 2:
            current_def = DEFINITIONS[token]
            
            while operator_stack:
                top_token = operator_stack[-1]
                if top_token == "(": break

                top_def = DEFINITIONS.get(top_token)
                if top_def is None: break 
                
                # Check precedence and associativity
                is_higher_precedence = current_def.precedence < top_def.precedence
                is_same_precedence_left_assoc = current_def.precedence == top_def.precedence and current_def.is_left_associative

                if is_higher_precedence or is_same_precedence_left_assoc:
                    output_queue.append(operator_stack.pop())
                else:
                    break
            operator_stack.append(token)
        # 4. Postfix operator '!'
        elif token == "!":
             output_queue.append(token)
        # 5. If the token is '('
        elif token == "(":
            operator_stack.append(token)
        # 6. If the token is ')'
        elif token == ")":
            while operator_stack and operator_stack[-1] != "(":
                output_queue.append(operator_stack.pop())
            if not operator_stack:
                raise ValueError("Mismatched parentheses in expression: missing '('")
            
            operator_stack.pop() # Pop the '('
            
            # Pop function if one is above '('
            if operator_stack and operator_stack[-1] in DEFINITIONS:
                output_queue.append(operator_stack.pop())
        else:
            raise ValueError(f"Invalid token found during parsing: {token}")

    # 7. Pop remaining operators
    while operator_stack:
        token = operator_stack.pop()
        if token == "(":
            raise ValueError("Mismatched parentheses in expression: missing ')'")
        
        output_queue.append(token)
    
    return output_queue

# --- 6. RPN Evaluation ---

def _evaluate_rpn(rpn_tokens):
    """Evaluates a list of RPN tokens."""
    value_stack = []

    for token in rpn_tokens:
        # 1. Number or 'p'
        if _is_number_or_constant(token):
            if token == "p":
                value_stack.append(math.pi)
            else:
                value_stack.append(float(token))
        # 2. Function or operator
        elif token in DEFINITIONS:
            def_ = DEFINITIONS[token]
            
            if def_.arity == 2: # Binary
                if len(value_stack) < 2: raise ValueError(f"Binary operator '{def_.name}' requires two operands.")
                b = value_stack.pop()
                a = value_stack.pop()
                value_stack.append(def_.func(a, b))
            
            elif def_.arity == 1: # Unary
                if not value_stack: raise ValueError(f"Unary operator '{def_.name}' requires one operand.")
                a = value_stack.pop()
                value_stack.append(def_.func(a))
        else:
            raise ValueError(f"Unknown token during evaluation: {token}")

        # Check for math domain errors (NaN, Infinity)
        if value_stack and (math.isnan(value_stack[-1]) or math.isinf(value_stack[-1])):
            raise ArithmeticError("Math domain error (e.g., log(-1)) or division by zero.")

    if len(value_stack) != 1:
        raise ValueError("Invalid RPN expression (too many/few operands).")

    return value_stack[0]

# --- 7. Main Execution Flow ---

def calculate(expression):
    """Main function to calculate the result of an infix expression string."""
    initialize_definitions()
    
    if not expression or not expression.strip():
        raise ValueError("Expression cannot be empty.")
    
    tokens = _tokenize(expression)
    rpn = _infix_to_rpn(tokens)
    
    # RPN DEBUG: print(f"RPN: {rpn}") 
    
    return _evaluate_rpn(rpn)

# --- 8. Main CLI Loop ---

def main():
    """Runs the command line interface loop."""
    print("--- Python CLI Calculator (Infix Mode) ---")
    print("Supported Operations:")
    print("  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)")
    print("  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S30)")
    print("  Unary (Postfix): ! (Factorial, e.g., 6!)")
    print("  Constant: p (PI)")
    print("\nNOTE: Use explicit multiplication (e.g., 2*(3) is correct).")
    print("Type 'exit' or 'quit' to end.\n")

    while True:
        try:
            input_str = input("Expression: ")
        except EOFError:
            break
        
        expression = input_str.strip()

        if not expression or expression.lower() in ('exit', 'quit'):
            break

        try:
            result = calculate(expression)
            print(f"Result: **{result:.10f}**\n")
        except (ValueError, ArithmeticError, TypeError) as ex:
            print(f"Error: Invalid expression. {ex}\n")
        except Exception as ex:
            print(f"An unexpected error occurred: {ex}\n")

    print("Exiting calculator. Goodbye!")

if __name__ == "__main__":
    main()