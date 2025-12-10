import math
import re
import sys

# ---- Preprocess expression for Python ----
def preprocess(expr):
    expr = expr.replace(" ", "")

    # Replace pi constant
    expr = expr.replace("p", str(math.pi))

    # Root operator a r b  →  b ** (1/a)
    expr = re.sub(r'(\d+)\s*r\s*(\d+)', r'(\2**(1/\1))', expr)

    # Factorial n! → factorial(n)
    expr = re.sub(r'(\d+)!', r'factorial(\1)', expr)

    # Functions
    expr = re.sub(r'\bS\(', "math.sin(", expr)
    expr = re.sub(r'\bs\(', "math.sinh(", expr)
    expr = re.sub(r'\bC\(', "math.cos(", expr)
    expr = re.sub(r'\bc\(', "math.cosh(", expr)
    expr = re.sub(r'\bT\(', "math.tan(", expr)
    expr = re.sub(r'\bh\(', "math.tanh(", expr)

    expr = re.sub(r'\bl\(', "math.log(", expr)       # ln
    expr = re.sub(r'\bL\(', "math.log10(", expr)     # log base 10

    # Power operator ^ → **
    expr = expr.replace("^", "**")

    return expr

# ---- Factorial manually ----
def factorial(n):
    if n < 0:
        raise ValueError("Negative factorial")
    result = 1
    for i in range(2, int(n) + 1):
        result *= i
    return result


# ---- Main CLI ----
def main():
    print("Python CLI Scientific Calculator (type 'exit' to quit)")

    while True:
        expr = input("> ")

        if expr.lower() == "exit":
            break

        try:
            processed = preprocess(expr)
            result = eval(processed, {"math": math, "factorial": factorial})
            print("=", result)
        except Exception as e:
            print("Error:", e)


if __name__ == "__main__":
    main()
