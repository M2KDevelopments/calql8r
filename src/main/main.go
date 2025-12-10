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

func factorial(n float64) float64 {
	if n < 0 {
		panic("Factorial not defined for negative numbers")
	}
	if n == 0 || n == 1 {
		return 1
	}

	result := 1.0
	for i := 2; i <= int(n); i++ {
		result *= float64(i)
	}
	return result
}

func evaluateExpression(input string) (float64, error) {
	expr := strings.ReplaceAll(input, " ", "")

	// Replace p with pi
	expr = strings.ReplaceAll(expr, "p", fmt.Sprintf("%f", math.Pi))

	// Handle factorials (n!)
	factorialRegex := regexp.MustCompile(`(\d+\.?\d*)!`)
	expr = factorialRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := factorialRegex.FindStringSubmatch(match)[1]
		num, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", factorial(num))
	})

	// Handle two-value functions (aFb format)

	// Pow (^): a^b
	powRegex := regexp.MustCompile(`(\d+\.?\d*)\^(\d+\.?\d*)`)
	expr = powRegex.ReplaceAllStringFunc(expr, func(match string) string {
		parts := powRegex.FindStringSubmatch(match)
		a, _ := strconv.ParseFloat(parts[1], 64)
		b, _ := strconv.ParseFloat(parts[2], 64)
		return fmt.Sprintf("%f", math.Pow(a, b))
	})

	// Root (r): arb means b-th root of a
	rootRegex := regexp.MustCompile(`(\d+\.?\d*)r(\d+\.?\d*)`)
	expr = rootRegex.ReplaceAllStringFunc(expr, func(match string) string {
		parts := rootRegex.FindStringSubmatch(match)
		a, _ := strconv.ParseFloat(parts[1], 64)
		b, _ := strconv.ParseFloat(parts[2], 64)
		return fmt.Sprintf("%f", math.Pow(a, 1.0/b))
	})

	// Handle one-value functions (Fn format)

	// Sin - S
	sinRegex := regexp.MustCompile(`S(\d+\.?\d*)`)
	expr = sinRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := sinRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Sin(n))
	})

	// SinH - s
	sinhRegex := regexp.MustCompile(`s(\d+\.?\d*)`)
	expr = sinhRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := sinhRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Sinh(n))
	})

	// Cos - C
	cosRegex := regexp.MustCompile(`C(\d+\.?\d*)`)
	expr = cosRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := cosRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Cos(n))
	})

	// CosH - c
	coshRegex := regexp.MustCompile(`c(\d+\.?\d*)`)
	expr = coshRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := coshRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Cosh(n))
	})

	// Tan - T
	tanRegex := regexp.MustCompile(`T(\d+\.?\d*)`)
	expr = tanRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := tanRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Tan(n))
	})

	// Tanh - t
	tanhRegex := regexp.MustCompile(`t(\d+\.?\d*)`)
	expr = tanhRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := tanhRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Tanh(n))
	})

	// Ln - l
	lnRegex := regexp.MustCompile(`l(\d+\.?\d*)`)
	expr = lnRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := lnRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Log(n))
	})

	// Log (base 10) - L
	logRegex := regexp.MustCompile(`L(\d+\.?\d*)`)
	expr = logRegex.ReplaceAllStringFunc(expr, func(match string) string {
		numStr := logRegex.FindStringSubmatch(match)[1]
		n, _ := strconv.ParseFloat(numStr, 64)
		return fmt.Sprintf("%f", math.Log10(n))
	})

	// Evaluate the final expression
	parser := NewParser(expr)
	return parser.ParseExpression()
}

type Parser struct {
	expr string
	pos  int
}

func NewParser(expr string) *Parser {
	return &Parser{expr: expr, pos: 0}
}

func (p *Parser) currentChar() byte {
	if p.pos < len(p.expr) {
		return p.expr[p.pos]
	}
	return 0
}

func (p *Parser) advance() {
	p.pos++
}

func (p *Parser) skipWhitespace() {
	for p.currentChar() != 0 && p.currentChar() == ' ' {
		p.advance()
	}
}

func (p *Parser) ParseExpression() (float64, error) {
	result, err := p.parseTerm()
	if err != nil {
		return 0, err
	}

	for {
		p.skipWhitespace()
		ch := p.currentChar()

		if ch == '+' {
			p.advance()
			term, err := p.parseTerm()
			if err != nil {
				return 0, err
			}
			result += term
		} else if ch == '-' {
			p.advance()
			term, err := p.parseTerm()
			if err != nil {
				return 0, err
			}
			result -= term
		} else {
			break
		}
	}

	return result, nil
}

func (p *Parser) parseTerm() (float64, error) {
	result, err := p.parseFactor()
	if err != nil {
		return 0, err
	}

	for {
		p.skipWhitespace()
		ch := p.currentChar()

		if ch == '*' {
			p.advance()
			factor, err := p.parseFactor()
			if err != nil {
				return 0, err
			}
			result *= factor
		} else if ch == '/' {
			p.advance()
			factor, err := p.parseFactor()
			if err != nil {
				return 0, err
			}
			result /= factor
		} else {
			break
		}
	}

	return result, nil
}

func (p *Parser) parseFactor() (float64, error) {
	p.skipWhitespace()

	// Handle negative numbers
	if p.currentChar() == '-' {
		p.advance()
		factor, err := p.parseFactor()
		if err != nil {
			return 0, err
		}
		return -factor, nil
	}

	// Handle positive sign
	if p.currentChar() == '+' {
		p.advance()
		return p.parseFactor()
	}

	// Handle parentheses
	if p.currentChar() == '(' {
		p.advance()
		result, err := p.ParseExpression()
		if err != nil {
			return 0, err
		}
		p.skipWhitespace()
		if p.currentChar() == ')' {
			p.advance()
		} else {
			return 0, fmt.Errorf("expected ')'")
		}
		return result, nil
	}

	// Handle numbers
	return p.parseNumber()
}

func (p *Parser) parseNumber() (float64, error) {
	p.skipWhitespace()
	start := p.pos

	for p.currentChar() != 0 && (p.currentChar() >= '0' && p.currentChar() <= '9' || p.currentChar() == '.') {
		p.advance()
	}

	if start == p.pos {
		return 0, fmt.Errorf("expected number at position %d", p.pos)
	}

	numStr := p.expr[start:p.pos]
	return strconv.ParseFloat(numStr, 64)
}

func main() {
	fmt.Println(strings.Repeat("=", 50))
	fmt.Println("CLI Calculator")
	fmt.Println(strings.Repeat("=", 50))
	fmt.Println("\nOperators:")
	fmt.Println("  S - Sin      s - SinH")
	fmt.Println("  C - Cos      c - CosH")
	fmt.Println("  T - Tan      t - TanH")
	fmt.Println("  ^ - Power    r - Root (arb = b-th root of a)")
	fmt.Println("  l - Ln       L - Log (base 10)")
	fmt.Println("  ! - Factorial")
	fmt.Println("  p - Pi (3.14159...)")
	fmt.Println("\nExamples:")
	fmt.Println("  1+2")
	fmt.Println("  2+5^4")
	fmt.Println("  2*(2+25)")
	fmt.Println("  6!")
	fmt.Println("  2+4^2")
	fmt.Println("  8r3 (cube root of 8)")
	fmt.Println("  L100 (log base 10 of 100)")
	fmt.Println("  S1.57 (sin of 1.57)")
	fmt.Println("\nType 'quit' or 'exit' to exit\n")
	fmt.Println(strings.Repeat("=", 50))

	scanner := bufio.NewScanner(os.Stdin)

	for {
		fmt.Print("\nEnter expression: ")
		if !scanner.Scan() {
			break
		}

		input := strings.TrimSpace(scanner.Text())

		// Check for exit commands
		if strings.ToLower(input) == "quit" || strings.ToLower(input) == "exit" || strings.ToLower(input) == "q" {
			fmt.Println("Goodbye!")
			break
		}

		// Skip empty input
		if input == "" {
			continue
		}

		// Evaluate and display result
		result, err := evaluateExpression(input)
		if err != nil {
			fmt.Printf("Error: %v\n", err)
		} else {
			fmt.Printf("Result: %f\n", result)
		}
	}

	if err := scanner.Err(); err != nil {
		fmt.Fprintf(os.Stderr, "Error reading input: %v\n", err)
	}
}
