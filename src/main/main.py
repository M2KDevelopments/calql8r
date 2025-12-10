import re
import math

def factorial(n):
    """Calculate factorial of n"""
    if n < 0:
        raise ValueError("Factorial not defined for negative numbers")
    if n == 0 or n == 1:
        return 1
    result = 1
    for i in range(2, int(n) + 1):
        result *= i
    return result

def evaluate_expression(expr):
    """Evaluate mathematical expression with custom notation"""
    # Remove spaces
    expr = expr.replace(' ', '')
    
    # Replace p with pi
    expr = expr.replace('p', str(math.pi))
    
    # Handle factorials (n!)
    def replace_factorial(match):
        num = match.group(1)
        return str(factorial(float(num)))
    
    expr = re.sub(r'(\d+\.?\d*)!', replace_factorial, expr)
    
    # Handle two-value functions (aFb format)
    # Pow (^): a^b
    def replace_pow(match):
        a, b = match.group(1), match.group(2)
        return str(float(a) ** float(b))
    
    expr = re.sub(r'(\d+\.?\d*)\^(\d+\.?\d*)', replace_pow, expr)
    
    # Root (r): arb means b-th root of a
    def replace_root(match):
        a, b = match.group(1), match.group(2)
        return str(float(a) ** (1 / float(b)))
    
    expr = re.sub(r'(\d+\.?\d*)r(\d+\.?\d*)', replace_root, expr)
    
    # Handle one-value functions (Fn format)
    # Sin - S
    def replace_sin(match):
        n = match.group(1)
        return str(math.sin(float(n)))
    
    expr = re.sub(r'S(\d+\.?\d*)', replace_sin, expr)
    
    # SinH - s
    def replace_sinh(match):
        n = match.group(1)
        return str(math.sinh(float(n)))
    
    expr = re.sub(r's(\d+\.?\d*)', replace_sinh, expr)
    
    # Cos - C
    def replace_cos(match):
        n = match.group(1)
        return str(math.cos(float(n)))
    
    expr = re.sub(r'C(\d+\.?\d*)', replace_cos, expr)
    
    # CosH - c
    def replace_cosh(match):
        n = match.group(1)
        return str(math.cosh(float(n)))
    
    expr = re.sub(r'c(\d+\.?\d*)', replace_cosh, expr)
    
    # Tan - T
    def replace_tan(match):
        n = match.group(1)
        return str(math.tan(float(n)))
    
    expr = re.sub(r'T(\d+\.?\d*)', replace_tan, expr)
    
    # Tanh - t
    def replace_tanh(match):
        n = match.group(1)
        return str(math.tanh(float(n)))
    
    expr = re.sub(r't(\d+\.?\d*)', replace_tanh, expr)
    
    # Ln - l
    def replace_ln(match):
        n = match.group(1)
        return str(math.log(float(n)))
    
    expr = re.sub(r'l(\d+\.?\d*)', replace_ln, expr)
    
    # Log (base 10) - L
    def replace_log(match):
        n = match.group(1)
        return str(math.log10(float(n)))
    
    expr = re.sub(r'L(\d+\.?\d*)', replace_log, expr)
    
    # Evaluate the final expression
    try:
        result = eval(expr)
        return result
    except Exception as e:
        raise ValueError(f"Error evaluating expression: {e}")

def main():
    """Main CLI calculator loop"""
    print("=" * 50)
    print("CLI Calculator")
    print("=" * 50)
    print("\nOperators:")
    print("  S - Sin      s - SinH")
    print("  C - Cos      c - CosH")
    print("  T - Tan      t - TanH")
    print("  ^ - Power    r - Root (arb = b-th root of a)")
    print("  l - Ln       L - Log (base 10)")
    print("  ! - Factorial")
    print("  p - Pi (3.14159...)")
    print("\nExamples:")
    print("  1+2")
    print("  2+5^4")
    print("  2*(2+25)")
    print("  6!")
    print("  2+4^2")
    print("  8r3 (cube root of 8)")
    print("  L100 (log base 10 of 100)")
    print("  S1.57 (sin of 1.57)")
    print("\nType 'quit' or 'exit' to exit\n")
    print("=" * 50)
    
    while True:
        try:
            # Get input from user
            expr = input("\nEnter expression: ").strip()
            
            # Check for exit commands
            if expr.lower() in ['quit', 'exit', 'q']:
                print("Goodbye!")
                break
            
            # Skip empty input
            if not expr:
                continue
            
            # Evaluate and display result
            result = evaluate_expression(expr)
            print(f"Result: {result}")
            
        except ValueError as e:
            print(f"Error: {e}")
        except KeyboardInterrupt:
            print("\n\nGoodbye!")
            break
        except Exception as e:
            print(f"Unexpected error: {e}")

if __name__ == "__main__":
    main()