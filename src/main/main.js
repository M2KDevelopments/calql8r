const readline = require('readline');

function factorial(n) {
    if (n < 0) throw new Error("Factorial not defined for negative numbers");
    if (n === 0 || n === 1) return 1;
    
    let result = 1;
    for (let i = 2; i <= Math.floor(n); i++) {
        result *= i;
    }
    return result;
}

function evaluateExpression(input) {
    let expr = input.replace(/\s+/g, '');
    
    // Replace p with pi
    expr = expr.replace(/p/g, Math.PI.toString());
    
    // Handle factorials (n!)
    expr = expr.replace(/(\d+\.?\d*)!/g, (match, num) => {
        return factorial(parseFloat(num)).toString();
    });
    
    // Handle two-value functions (aFb format)
    
    // Pow (^): a^b
    expr = expr.replace(/(\d+\.?\d*)\^(\d+\.?\d*)/g, (match, a, b) => {
        return Math.pow(parseFloat(a), parseFloat(b)).toString();
    });
    
    // Root (r): arb means b-th root of a
    expr = expr.replace(/(\d+\.?\d*)r(\d+\.?\d*)/g, (match, a, b) => {
        return Math.pow(parseFloat(a), 1 / parseFloat(b)).toString();
    });
    
    // Handle one-value functions (Fn format)
    
    // Sin - S
    expr = expr.replace(/S(\d+\.?\d*)/g, (match, n) => {
        return Math.sin(parseFloat(n)).toString();
    });
    
    // SinH - s
    expr = expr.replace(/s(\d+\.?\d*)/g, (match, n) => {
        return Math.sinh(parseFloat(n)).toString();
    });
    
    // Cos - C
    expr = expr.replace(/C(\d+\.?\d*)/g, (match, n) => {
        return Math.cos(parseFloat(n)).toString();
    });
    
    // CosH - c
    expr = expr.replace(/c(\d+\.?\d*)/g, (match, n) => {
        return Math.cosh(parseFloat(n)).toString();
    });
    
    // Tan - T
    expr = expr.replace(/T(\d+\.?\d*)/g, (match, n) => {
        return Math.tan(parseFloat(n)).toString();
    });
    
    // Tanh - t
    expr = expr.replace(/t(\d+\.?\d*)/g, (match, n) => {
        return Math.tanh(parseFloat(n)).toString();
    });
    
    // Ln - l
    expr = expr.replace(/l(\d+\.?\d*)/g, (match, n) => {
        return Math.log(parseFloat(n)).toString();
    });
    
    // Log (base 10) - L
    expr = expr.replace(/L(\d+\.?\d*)/g, (match, n) => {
        return Math.log10(parseFloat(n)).toString();
    });
    
    // Evaluate the final expression
    return evaluateBasicExpression(expr);
}

function evaluateBasicExpression(expr) {
    // Simple recursive descent parser for +, -, *, /, ()
    return new Parser(expr).parseExpression();
}

class Parser {
    constructor(expr) {
        this.expr = expr;
        this.pos = 0;
    }
    
    currentChar() {
        return this.pos < this.expr.length ? this.expr[this.pos] : null;
    }
    
    advance() {
        this.pos++;
    }
    
    skipWhitespace() {
        while (this.currentChar() && /\s/.test(this.currentChar())) {
            this.advance();
        }
    }
    
    parseExpression() {
        let result = this.parseTerm();
        
        while (true) {
            this.skipWhitespace();
            const ch = this.currentChar();
            
            if (ch === '+') {
                this.advance();
                result += this.parseTerm();
            } else if (ch === '-') {
                this.advance();
                result -= this.parseTerm();
            } else {
                break;
            }
        }
        
        return result;
    }
    
    parseTerm() {
        let result = this.parseFactor();
        
        while (true) {
            this.skipWhitespace();
            const ch = this.currentChar();
            
            if (ch === '*') {
                this.advance();
                result *= this.parseFactor();
            } else if (ch === '/') {
                this.advance();
                result /= this.parseFactor();
            } else {
                break;
            }
        }
        
        return result;
    }
    
    parseFactor() {
        this.skipWhitespace();
        
        // Handle negative numbers
        if (this.currentChar() === '-') {
            this.advance();
            return -this.parseFactor();
        }
        
        // Handle positive sign
        if (this.currentChar() === '+') {
            this.advance();
            return this.parseFactor();
        }
        
        // Handle parentheses
        if (this.currentChar() === '(') {
            this.advance();
            const result = this.parseExpression();
            this.skipWhitespace();
            if (this.currentChar() === ')') {
                this.advance();
            } else {
                throw new Error("Expected ')'");
            }
            return result;
        }
        
        // Handle numbers
        return this.parseNumber();
    }
    
    parseNumber() {
        this.skipWhitespace();
        const start = this.pos;
        
        while (this.currentChar() && (/\d/.test(this.currentChar()) || this.currentChar() === '.')) {
            this.advance();
        }
        
        if (start === this.pos) {
            throw new Error(`Expected number at position ${this.pos}`);
        }
        
        return parseFloat(this.expr.substring(start, this.pos));
    }
}

function main() {
    console.log("=".repeat(50));
    console.log("CLI Calculator");
    console.log("=".repeat(50));
    console.log("\nOperators:");
    console.log("  S - Sin      s - SinH");
    console.log("  C - Cos      c - CosH");
    console.log("  T - Tan      t - TanH");
    console.log("  ^ - Power    r - Root (arb = b-th root of a)");
    console.log("  l - Ln       L - Log (base 10)");
    console.log("  ! - Factorial");
    console.log("  p - Pi (3.14159...)");
    console.log("\nExamples:");
    console.log("  1+2");
    console.log("  2+5^4");
    console.log("  2*(2+25)");
    console.log("  6!");
    console.log("  2+4^2");
    console.log("  8r3 (cube root of 8)");
    console.log("  L100 (log base 10 of 100)");
    console.log("  S1.57 (sin of 1.57)");
    console.log("\nType 'quit' or 'exit' to exit\n");
    console.log("=".repeat(50));
    
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });
    
    const promptUser = () => {
        rl.question('\nEnter expression: ', (input) => {
            const trimmedInput = input.trim();
            
            // Check for exit commands
            if (['quit', 'exit', 'q'].includes(trimmedInput.toLowerCase())) {
                console.log("Goodbye!");
                rl.close();
                return;
            }
            
            // Skip empty input
            if (trimmedInput === '') {
                promptUser();
                return;
            }
            
            try {
                const result = evaluateExpression(trimmedInput);
                console.log(`Result: ${result}`);
            } catch (e) {
                console.log(`Error: ${e.message}`);
            }
            
            promptUser();
        });
    };
    
    promptUser();
}

// Run the calculator
main();