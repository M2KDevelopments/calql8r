package main

import (
	"bufio"
	"fmt"
	"math"
	"os"
	"strconv"
	"unicode"
)

type Parser struct {
	text string
	pos  int
}

func NewParser(input string) *Parser {
	return &Parser{text: input}
}

func (p *Parser) peek() rune {
	if p.pos >= len(p.text) {
		return 0
	}
	return rune(p.text[p.pos])
}

func (p *Parser) next() rune {
	if p.pos >= len(p.text) {
		return 0
	}
	ch := rune(p.text[p.pos])
	p.pos++
	return ch
}

func (p *Parser) eat(ch rune) bool {
	if p.peek() == ch {
		p.pos++
		return true
	}
	return false
}

// --------- Grammar: expression → term ((+|-) term)* ----------
func (p *Parser) parseExpression() float64 {
	value := p.parseTerm()

	for {
		if p.eat('+') {
			value += p.parseTerm()
		} else if p.eat('-') {
			value -= p.parseTerm()
		} else {
			return value
		}
	}
}

// --------- term → factor ((*|/) factor)* ----------
func (p *Parser) parseTerm() float64 {
	value := p.parseFactor()

	for {
		if p.eat('*') {
			value *= p.parseFactor()
		} else if p.eat('/') {
			value /= p.parseFactor()
		} else {
			return value
		}
	}
}

// --------- factor → unary (^ unary | r unary)* ----------
func (p *Parser) parseFactor() float64 {
	value := p.parseUnary()

	for {
		if p.eat('^') {
			value = math.Pow(value, p.parseUnary())
		} else if p.eat('r') {
			right := p.parseUnary()
			value = math.Pow(right, 1.0/value) // a r b = b^(1/a)
		} else {
			return value
		}
	}
}

// --------- unary → (+|-) unary | primary ----------
func (p *Parser) parseUnary() float64 {
	if p.eat('+') {
		return p.parseUnary()
	}
	if p.eat('-') {
		return -p.parseUnary()
	}
	return p.parsePrimary()
}

// --------- primary → number | function primary | '(' expression ')' ----------
func (p *Parser) parsePrimary() float64 {
	ch := p.peek()

	// Function: S s C c T h l L r
	if unicode.IsLetter(ch) {
		fn := p.readFunction()
		arg := p.parsePrimary()
		return applyFunction(fn, arg)
	}

	// Parentheses
	if p.eat('(') {
		value := p.parseExpression()
		if !p.eat(')') {
			panic("missing closing )")
		}
		if p.eat('!') {
			return factorial(value)
		}
		return value
	}

	// Numbers or 'p'
	value := p.parseNumber()
	if p.eat('!') {
		return factorial(value)
	}

	return value
}

// Reads a function name (letter sequence)
func (p *Parser) readFunction() string {
	fn := ""
	for unicode.IsLetter(p.peek()) {
		fn += string(p.next())
	}
	return fn
}

// Parse number or π constant
func (p *Parser) parseNumber() float64 {
	if p.peek() == 'p' {
		p.next()
		return math.Pi
	}

	num := ""
	for unicode.IsDigit(p.peek()) || p.peek() == '.' {
		num += string(p.next())
	}

	if num == "" {
		panic("number expected")
	}

	v, _ := strconv.ParseFloat(num, 64)
	return v
}

func applyFunction(fn string, x float64) float64 {
	switch fn {
	case "S":
		return math.Sin(x)
	case "s":
		return math.Sinh(x)
	case "C":
		return math.Cos(x)
	case "c":
		return math.Cosh(x)
	case "T":
		return math.Tan(x)
	case "h":
		return math.Tanh(x)
	case "l":
		return math.Log(x)
	case "L":
		return math.Log10(x)
	case "r":
		return math.Sqrt(x)
	default:
		panic("unknown function: " + fn)
	}
}

// Simple factorial
func factorial(n float64) float64 {
	if n < 0 {
		panic("factorial of negative number")
	}
	res := 1.0
	for i := 2; i <= int(n); i++ {
		res *= float64(i)
	}
	return res
}

func main() {
	reader := bufio.NewReader(os.Stdin)
	fmt.Println("Go CLI Scientific Calculator (type 'exit' to quit)")

	for {
		fmt.Print("> ")
		input, _ := reader.ReadString('\n')
		input = input[:len(input)-1]

		if input == "exit" {
			break
		}

		parser := NewParser(input)

		// Remove spaces
		noSpace := ""
		for _, c := range parser.text {
			if c != ' ' {
				noSpace += string(c)
			}
		}
		parser.text = noSpace

		defer func() {
			if r := recover(); r != nil {
				fmt.Println("Error:", r)
			}
		}()

		result := parser.parseExpression()
		fmt.Println("= ", result)
	}
}
