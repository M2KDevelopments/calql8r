package main

import (
	"bufio"
	"fmt"
	"math"
	"os"
	"regexp"
	"strconv"
	"strings"
)

// --- 1. Operator and Function Definitions ---

// Define custom function types
type BinaryFunction func(a, b float64) float64
type UnaryFunction func(a float64) float64

// OpFuncDef represents an operator or function with its properties.
type OpFuncDef struct {
	Name              string
	Precedence        int
	IsLeftAssociative bool
	Arity             int         // 1 for unary, 2 for binary
	Function          interface{} // Function can be BinaryFunction or UnaryFunction
}

// Global static map for all known operators and functions
var definitions = make(map[string]OpFuncDef)

// --- 2. Function Implementations ---

// Binary Functions
func _add(a, b float64) float64      { return a + b }
func _subtract(a, b float64) float64 { return a - b }
func _multiply(a, b float64) float64 { return a * b }
func _divide(a, b float64) float64   { return a / b }
func _power(a, b float64) float64    { return math.Pow(a, b) }

// Custom Root: a r b means b-th root of a. (Root degree is b)
func _root(a, b float64) float64 {
	if a < 0 && math.Mod(b, 2.0) == 0.0 {
		panic("Cannot take even root of a negative number.")
	}
	return math.Pow(a, 1.0/b)
}

// Unary Functions
func _sin(a float64) float64   { return math.Sin(a) }
func _sinh(a float64) float64  { return math.Sinh(a) }
func _cos(a float64) float64   { return math.Cos(a) }
func _cosh(a float64) float64  { return math.Cosh(a) }
func _tan(a float64) float64   { return math.Tan(a) }
func _tanh(a float64) float64  { return math.Tanh(a) }
func _ln(a float64) float64    { return math.Log(a) } // Natural log
func _log10(a float64) float64 { return math.Log10(a) }

// Unary Post-fix Factorial
func _factorial(a float64) float64 {
	if a < 0 || math.Mod(a, 1.0) != 0.0 {
		panic("Factorial only defined for non-negative integers.")
	}
	if a == 0 {
		return 1.0
	}
	res := 1.0
	for i := 1; i <= int(a); i++ {
		res *= float64(i)
	}
	return res
}

// --- 3. Initialization ---

func init() {
	// Precedence: Higher number binds tighter

	// Postfix Unary (Highest Precedence)
	definitions["!"] = OpFuncDef{Name: "!", Precedence: 6, IsLeftAssociative: true, Arity: 1, Function: UnaryFunction(_factorial)}

	// Prefix Unary Functions (Right Associative precedence 5)
	definitions["S"] = OpFuncDef{Name: "S", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_sin)}
	definitions["s"] = OpFuncDef{Name: "s", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_sinh)}
	definitions["C"] = OpFuncDef{Name: "C", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_cos)}
	definitions["c"] = OpFuncDef{Name: "c", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_cosh)}
	definitions["T"] = OpFuncDef{Name: "T", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_tan)}
	definitions["t"] = OpFuncDef{Name: "t", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_tanh)}
	definitions["l"] = OpFuncDef{Name: "l", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_ln)}
	definitions["L"] = OpFuncDef{Name: "L", Precedence: 5, IsLeftAssociative: false, Arity: 1, Function: UnaryFunction(_log10)}

	// Binary Operators
	definitions["^"] = OpFuncDef{Name: "^", Precedence: 4, IsLeftAssociative: false, Arity: 2, Function: BinaryFunction(_power)}
	definitions["r"] = OpFuncDef{Name: "r", Precedence: 4, IsLeftAssociative: true, Arity: 2, Function: BinaryFunction(_root)}

	definitions["*"] = OpFuncDef{Name: "*", Precedence: 3, IsLeftAssociative: true, Arity: 2, Function: BinaryFunction(_multiply)}
	definitions["/"] = OpFuncDef{Name: "/", Precedence: 3, IsLeftAssociative: true, Arity: 2, Function: BinaryFunction(_divide)}

	definitions["+"] = OpFuncDef{Name: "+", Precedence: 2, IsLeftAssociative: true, Arity: 2, Function: BinaryFunction(_add)}
	// We use an internal token, 'BIN_SUB', for binary subtraction to distinguish it from unary minus.
	definitions["BIN_SUB"] = OpFuncDef{Name: "-", Precedence: 2, IsLeftAssociative: true, Arity: 2, Function: BinaryFunction(_subtract)}
}

// --- 4. Lexer/Tokenizer ---

// Regex to capture the different token types. This simplifies the process greatly.
var tokenRegex = regexp.MustCompile(`(\d+\.?\d*|\.\d+)|([SsCcTtLlr\^\*\/\+\-\!\(\)])|p`)

// _tokenize converts the raw input string into a list of tokens.
func _tokenize(expression string) []string {
	// 1. Preprocess: Insert spaces around all single-character tokens (except inside numbers)
	processed := expression
	tokensToSeparate := []string{"(", ")", "+", "*", "/", "^", "!", "r", "S", "s", "C", "c", "T", "t", "l", "L", "p"}
	for _, sep := range tokensToSeparate {
		processed = strings.ReplaceAll(processed, sep, " "+sep+" ")
	}

	// Separate '-' which needs special handling
	processed = strings.ReplaceAll(processed, "-", " - ")

	// Clean up multiple spaces and trim
	tokens := strings.Fields(processed)

	// 2. Handle Unary Minus vs. Binary Minus
	resultTokens := []string{}
	for i, token := range tokens {
		if token == "-" {
			// Check if the preceding token is an operand (number, constant, '!', or ')')
			isPrecedingTokenOperand := i > 0 &&
				(len(resultTokens) > 0 && (isNumber(resultTokens[len(resultTokens)-1]) || resultTokens[len(resultTokens)-1] == "p" || resultTokens[len(resultTokens)-1] == "!" || resultTokens[len(resultTokens)-1] == ")"))

			if i == 0 || !isPrecedingTokenOperand {
				// Unary Minus: Merge it with the next token
				if i+1 < len(tokens) {
					tokens[i+1] = token + tokens[i+1]
				}
				// Skip the current token as it's merged into the next
			} else {
				// Binary Minus: Use the internal token
				resultTokens = append(resultTokens, "BIN_SUB")
			}
		} else {
			resultTokens = append(resultTokens, token)
		}
	}
	return resultTokens
}

// isNumber is a simple helper to check if a string is a number (including negative numbers merged by the tokenizer).
func isNumber(s string) bool {
	_, err := strconv.ParseFloat(s, 64)
	return err == nil
}

// --- 5. Shunting-Yard Algorithm (Infix to RPN) ---

// _infixToRpn converts a list of infix tokens to a list of RPN tokens.
func _infixToRpn(infixTokens []string) ([]string, error) {
	outputQueue := []string{}
	operatorStack := []string{}

	for _, token := range infixTokens {
		// 1. If the token is a number or constant 'p', add it to the output queue.
		if isNumber(token) || token == "p" {
			outputQueue = append(outputQueue, token)
		} else if def, exists := definitions[token]; exists {
			// 2. If the token is a function or prefix unary operator
			if def.Arity == 1 && token != "!" {
				operatorStack = append(operatorStack, token)
			} else if def.Arity == 2 {
				// 3. If the token is a binary operator
				for len(operatorStack) > 0 {
					topToken := operatorStack[len(operatorStack)-1]
					if topToken == "(" {
						break
					}

					if topDef, ok := definitions[topToken]; ok {
						// Check precedence and associativity
						isHigherPrecedence := def.Precedence < topDef.Precedence
						isSamePrecedenceLeftAssoc := def.Precedence == topDef.Precedence && def.IsLeftAssociative

						if isHigherPrecedence || isSamePrecedenceLeftAssoc {
							outputQueue = append(outputQueue, topToken)
							operatorStack = operatorStack[:len(operatorStack)-1] // Pop
						} else {
							break
						}
					} else {
						// Should only hit '('
						break
					}
				}
				operatorStack = append(operatorStack, token)
			} else if token == "!" {
				// 4. If the token is a postfix operator '!'
				outputQueue = append(outputQueue, token)
			}
		} else if token == "(" {
			// 5. If the token is '(', push it onto the operator stack.
			operatorStack = append(operatorStack, token)
		} else if token == ")" {
			// 6. If the token is ')', pop operators until '(' is found.
			for len(operatorStack) > 0 && operatorStack[len(operatorStack)-1] != "(" {
				outputQueue = append(outputQueue, operatorStack[len(operatorStack)-1])
				operatorStack = operatorStack[:len(operatorStack)-1] // Pop
			}
			if len(operatorStack) == 0 {
				return nil, fmt.Errorf("mismatched parentheses: missing '('")
			}
			operatorStack = operatorStack[:len(operatorStack)-1] // Pop the '('

			// If there is a function token at the top of the stack, pop it.
			if len(operatorStack) > 0 {
				topToken := operatorStack[len(operatorStack)-1]
				if _, ok := definitions[topToken]; ok {
					outputQueue = append(outputQueue, topToken)
					operatorStack = operatorStack[:len(operatorStack)-1] // Pop function
				}
			}
		} else {
			return nil, fmt.Errorf("invalid token during parsing: %s", token)
		}
	}

	// 7. Pop remaining operators from stack to RPN output.
	for len(operatorStack) > 0 {
		token := operatorStack[len(operatorStack)-1]
		if token == "(" {
			return nil, fmt.Errorf("mismatched parentheses: missing ')'")
		}
		outputQueue = append(outputQueue, token)
		operatorStack = operatorStack[:len(operatorStack)-1]
	}

	return outputQueue, nil
}

// --- 6. RPN Evaluation ---

// _evaluateRpn evaluates a list of RPN tokens.
func _evaluateRpn(rpnTokens []string) (float64, error) {
	valueStack := []float64{}

	for _, token := range rpnTokens {
		// 1. If the token is a number or constant 'p', push the value onto the stack.
		if isNumber(token) {
			num, _ := strconv.ParseFloat(token, 64)
			valueStack = append(valueStack, num)
		} else if token == "p" {
			valueStack = append(valueStack, math.Pi)
		} else if def, exists := definitions[token]; exists {
			// 2. If the token is a function or operator.
			if def.Arity == 2 { // Binary
				if len(valueStack) < 2 {
					return 0, fmt.Errorf("binary operator '%s' requires two operands", def.Name)
				}
				b := valueStack[len(valueStack)-1]
				a := valueStack[len(valueStack)-2]
				valueStack = valueStack[:len(valueStack)-2] // Pop two

				result := def.Function.(BinaryFunction)(a, b)
				if math.IsNaN(result) || math.IsInf(result, 0) {
					return result, fmt.Errorf("math domain error or division by zero")
				}
				valueStack = append(valueStack, result)

			} else if def.Arity == 1 { // Unary
				if len(valueStack) < 1 {
					return 0, fmt.Errorf("unary operator '%s' requires one operand", def.Name)
				}
				a := valueStack[len(valueStack)-1]
				valueStack = valueStack[:len(valueStack)-1] // Pop one

				result := def.Function.(UnaryFunction)(a)
				if math.IsNaN(result) || math.IsInf(result, 0) {
					return result, fmt.Errorf("math domain error or division by zero")
				}
				valueStack = append(valueStack, result)
			}
		} else {
			return 0, fmt.Errorf("unknown token during evaluation: %s", token)
		}
	}

	if len(valueStack) != 1 {
		return 0, fmt.Errorf("invalid RPN expression (too many/few operands)")
	}

	return valueStack[0], nil
}

// --- 7. Main Execution Flow ---

// Calculate is the main entry point to evaluate an infix expression string.
func Calculate(expression string) (float64, error) {
	if strings.TrimSpace(expression) == "" {
		return 0, fmt.Errorf("empty expression")
	}

	// 1. Tokenize
	tokens := _tokenize(expression)

	// 2. Infix to RPN (Shunting-Yard)
	rpn, err := _infixToRpn(tokens)
	if err != nil {
		return 0, err
	}

	// DEBUG: Print RPN
	// fmt.Printf("RPN: %v\n", rpn)

	// 3. Evaluate RPN
	result, err := _evaluateRpn(rpn)
	if err != nil {
		return 0, err
	}

	return result, nil
}

// --- 8. Main CLI Loop ---

func main() {
	fmt.Println("--- Go CLI Calculator (Infix Mode) ---")
	fmt.Println("Supported Operations:")
	fmt.Println("  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)")
	fmt.Println("  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S30)")
	fmt.Println("  Unary (Postfix): ! (Factorial, e.g., 6!)")
	fmt.Println("  Constant: p (PI)")
	fmt.Println("\nNOTE: Use explicit multiplication (e.g., 2*(3) is correct).")
	fmt.Println("Type 'exit' or 'quit' to end.\n")

	reader := bufio.NewReader(os.Stdin)

	for {
		fmt.Print("Expression: ")
		input, _ := reader.ReadString('\n')
		input = strings.TrimSpace(input)

		if input == "" || strings.ToLower(input) == "exit" || strings.ToLower(input) == "quit" {
			break
		}

		// Use a panic/recover block to handle math domain errors (like log(-1))
		func() {
			defer func() {
				if r := recover(); r != nil {
					fmt.Printf("Error: Runtime exception occurred. %v\n\n", r)
				}
			}()

			result, err := Calculate(input)
			if err != nil {
				fmt.Printf("Error: Invalid expression. %v\n\n", err)
				return
			}
			fmt.Printf("Result: **%.10f**\n\n", result)
		}()
	}

	fmt.Println("Exiting calculator. Goodbye!")
}
