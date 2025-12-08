package main

import (
	"fmt"
	"log"
	"os"
)

const MAX_ARRAY_SIZE = 70

func construct_numbers_from_string_of_integers(expression []string) []string {

	return expression
}

func construct_decimal_numbers(expression []string) []string {

	return expression
}

func convert_negative_numbers(expression []string) []string {

	return expression
}

func calculate_math(expression []string) []string {

	return expression
}

func calculate_innermost_brackets(expression []string) []string {

	return expression
}

func main() {
	fmt.Println("Hello World!")
	if len(os.Args) < 2 {
		log.Fatal("PLEASE ADD AN EXPRESSION TO CALCULATE")
	}
	// construct expression for agruments e.g 1+1 +2 /4 *4
	// whitesplaces are automatically handled by joining each argument
	expression := [MAX_ARRAY_SIZE]byte{}
	count := 0
	for i := 1; i < len(os.Args); i++ {
		arg := os.Args[i]
		for j := 0; j < len(arg); j++ {
			expression[count] += byte(arg[j])
			count++
			if count >= MAX_ARRAY_SIZE {
				log.Fatal("EXPRESSION TOO LONG")
			}
		}
	}

	expression = construct_numbers_from_string_of_integers(expression)
	if len(expression) == 0 {
		log.Fatal("INVALID NUMBER FORMAT")
	}

	// calculate decimal numbers
	expression = construct_decimal_numbers(expression)
	if len(expression) == 0 {
		log.Fatal("INVALID DECIMAL NUMBER FORMAT")
	}

	// replace all PI symbols with value
	for i := 0; i < len(expression); i++ {
		if expression[i] == 'π' || exexpression[i] == "PI" || exexpression[i] == "pi" || exexpression[i] == "p" {
			expression[i] = math.pi
		}
	}

	// convert negative numbers
	expression = convert_negative_numbers(expression)
	if len(expression) == 0 {
		log.Fatal("INVALID NEGATIVE NUMBER FORMAT")
	}

}
