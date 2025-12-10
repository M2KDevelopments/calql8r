/**
 * CLI Calculator Program (Node.js)
 * Implements the Shunting-Yard Algorithm to convert infix expressions to RPN 
 * and then evaluates the RPN using a stack.
 */

// --- 1. Operator and Function Definitions ---

// Define function types for clarity (although JS uses generic functions)
// type BinaryFunction = (a, b) => number;
// type UnaryFunction = (a) => number;

/**
 * Represents an operator or function with its properties for the Shunting-Yard algorithm.
 */
class OpFuncDef {
    constructor(name, precedence, isLeftAssociative, arity, func) {
        this.name = name;
        this.precedence = precedence;
        this.isLeftAssociative = isLeftAssociative;
        this.arity = arity;
        this.func = func; // The actual JavaScript function
    }
}

// Global static map for all known operators and functions
const DEFINITIONS = new Map();

// --- 2. Function Implementations ---

// Custom Root: a r b means b-th root of a. (Root degree is b)
const root = (a, b) => {
    if (a < 0 && Math.abs(b % 2) < 1e-9) // Check if b is an even integer
        throw new Error("Cannot take even root of a negative number.");
    return Math.pow(a, 1.0 / b);
};

// Unary Post-fix Factorial
const factorial = (a) => {
    if (a < 0 || Math.abs(a % 1) > 1e-9)
        throw new Error("Factorial only defined for non-negative integers.");
    if (a === 0) return 1;
    
    let res = 1;
    for (let i = 1; i <= a; i++) {
        res *= i;
    }
    return res;
};

// --- 3. Static Initialization ---

function initializeDefinitions() {
    if (DEFINITIONS.size > 0) return;

    // Precedence: Higher number binds tighter
    
    // Postfix Unary (Highest Precedence)
    DEFINITIONS.set("!", new OpFuncDef("!", 6, true, 1, factorial)); 

    // Prefix Unary Functions (Right Associative precedence 5)
    DEFINITIONS.set("S", new OpFuncDef("S", 5, false, 1, Math.sin));
    DEFINITIONS.set("s", new OpFuncDef("s", 5, false, 1, Math.sinh));
    DEFINITIONS.set("C", new OpFuncDef("C", 5, false, 1, Math.cos));
    DEFINITIONS.set("c", new OpFuncDef("c", 5, false, 1, Math.cosh));
    DEFINITIONS.set("T", new OpFuncDef("T", 5, false, 1, Math.tan));
    DEFINITIONS.set("t", new OpFuncDef("t", 5, false, 1, Math.tanh));
    DEFINITIONS.set("l", new OpFuncDef("l", 5, false, 1, Math.log));    // Ln (Natural Log)
    DEFINITIONS.set("L", new OpFuncDef("L", 5, false, 1, Math.log10)); // Log10

    // Binary Operators
    DEFINITIONS.set("^", new OpFuncDef("^", 4, false, 2, Math.pow)); // Right Associative
    DEFINITIONS.set("r", new OpFuncDef("r", 4, true, 2, root));       // Left Associative

    DEFINITIONS.set("*", new OpFuncDef("*", 3, true, 2, (a, b) => a * b));
    DEFINITIONS.set("/", new OpFuncDef("/", 3, true, 2, (a, b) => a / b));
    
    DEFINITIONS.set("+", new OpFuncDef("+", 2, true, 2, (a, b) => a + b));
    // Internal token '_' for binary subtraction, distinguishing it from unary minus.
    DEFINITIONS.set("_", new OpFuncDef("-", 2, true, 2, (a, b) => a - b)); 
}

// --- 4. Lexer/Tokenizer ---

/**
 * Converts the raw input string into a list of tokens, handling unary minus.
 */
function tokenize(expression) {
    // 1. Preprocess: Insert spaces around tokens for easier splitting
    let processed = expression.replace(/\s+/g, ""); // Remove existing spaces

    const separators = ["(", ")", "+", "*", "/", "^", "!", "r", "S", "s", "C", "c", "T", "t", "l", "L", "p"];
    for (const sep of separators) {
        processed = processed.replaceAll(sep, ` ${sep} `);
    }

    // Separate minus sign, which is handled specially
    processed = processed.replaceAll("-", " - ");

    // Clean up multiple spaces and split
    const tokens = processed.split(/\s+/).filter(t => t.length > 0);

    // 2. Handle Unary Minus vs. Binary Minus
    const resultTokens = [];
    
    for (let i = 0; i < tokens.length; i++) {
        const token = tokens[i];
        
        if (token === "-") {
            // Check if the preceding token is an operand (number, constant, '!', or ')')
            const lastResultToken = resultTokens[resultTokens.length - 1];
            const isPrecedingTokenOperand = resultTokens.length > 0 && 
                (!isNaN(parseFloat(lastResultToken)) || lastResultToken === "!" || lastResultToken === ")" || lastResultToken === "p");
            
            if (i === 0 || !isPrecedingTokenOperand) {
                // Unary Minus: Merge it with the next token (e.g., "-5")
                if (i + 1 < tokens.length) {
                    tokens[i + 1] = token + tokens[i + 1];
                }
                // Skip the current token as it's merged into the next
            } else {
                // Binary Minus: Use the internal token '_'
                resultTokens.push("_");
            }
        } else {
            resultTokens.push(token);
        }
    }
    return resultTokens;
}

function isNumberOrConstant(s) {
    if (s === "p") return true;
    return !isNaN(parseFloat(s));
}

// --- 5. Shunting-Yard Algorithm (Infix to RPN) ---

/**
 * Converts a list of infix tokens to a list of RPN tokens using Shunting-Yard.
 */
function infixToRpn(infixTokens) {
    const outputQueue = [];
    const operatorStack = [];
    
    for (const token of infixTokens) {
        // 1. Number or constant 'p'
        if (isNumberOrConstant(token)) {
            outputQueue.push(token);
        }
        // 2. Function or prefix unary operator (not '!')
        else if ((DEFINITIONS.has(token) && DEFINITIONS.get(token).arity === 1 && token !== "!") || token === "(") {
            operatorStack.push(token);
        }
        // 3. Binary operator ('_' is binary subtraction)
        else if (DEFINITIONS.has(token) && DEFINITIONS.get(token).arity === 2) {
            const currentDef = DEFINITIONS.get(token);
            
            while (operatorStack.length > 0) {
                const topToken = operatorStack[operatorStack.length - 1];
                if (topToken === "(") break;

                const topDef = DEFINITIONS.get(topToken);
                if (!topDef) break; 
                
                // Check precedence and associativity
                const isHigherPrecedence = currentDef.precedence < topDef.precedence;
                const isSamePrecedenceLeftAssoc = currentDef.precedence === topDef.precedence && currentDef.isLeftAssociative;

                if (isHigherPrecedence || isSamePrecedenceLeftAssoc) {
                    outputQueue.push(operatorStack.pop());
                } else {
                    break;
                }
            }
            operatorStack.push(token);
        }
        // 4. Postfix operator '!'
        else if (token === "!") {
             outputQueue.push(token);
        }
        // 5. If the token is ')'
        else if (token === ")") {
            while (operatorStack.length > 0 && operatorStack[operatorStack.length - 1] !== "(") {
                outputQueue.push(operatorStack.pop());
            }
            if (operatorStack.length === 0)
                throw new Error("Mismatched parentheses in expression: missing '('");
            
            operatorStack.pop(); // Pop the '('
            
            // Pop function if one is above '('
            if (operatorStack.length > 0 && DEFINITIONS.has(operatorStack[operatorStack.length - 1])) {
                outputQueue.push(operatorStack.pop());
            }
        }
        else {
            throw new Error(`Invalid token found during parsing: ${token}`);
        }
    }

    // 6. Pop remaining operators
    while (operatorStack.length > 0) {
        const token = operatorStack.pop();
        if (token === "(")
            throw new Error("Mismatched parentheses in expression: missing ')'");
        
        outputQueue.push(token);
    }
    
    return outputQueue;
}

// --- 6. RPN Evaluation ---

/**
 * Evaluates a list of RPN tokens.
 */
function evaluateRpn(rpnTokens) {
    const valueStack = [];

    for (const token of rpnTokens) {
        // 1. Number or 'p'
        if (isNumberOrConstant(token)) {
            if (token === "p") {
                valueStack.push(Math.PI);
            } else {
                valueStack.push(parseFloat(token));
            }
        }
        // 2. Function or operator
        else if (DEFINITIONS.has(token)) {
            const def = DEFINITIONS.get(token);
            
            if (def.arity === 2) { // Binary
                if (valueStack.length < 2) throw new Error(`Binary operator '${def.name}' requires two operands.`);
                const b = valueStack.pop();
                const a = valueStack.pop();
                valueStack.push(def.func(a, b));
            }
            else if (def.arity === 1) { // Unary
                if (valueStack.length < 1) throw new Error(`Unary operator '${def.name}' requires one operand.`);
                const a = valueStack.pop();
                valueStack.push(def.func(a));
            }
        }
        else {
            throw new Error(`Unknown token during evaluation: ${token}`);
        }

        // Check for math domain errors (NaN, Infinity)
        if (valueStack.length > 0) {
            const result = valueStack[valueStack.length - 1];
            if (isNaN(result) || !isFinite(result)) {
                throw new Error("Math domain error (e.g., log(-1)) or division by zero.");
            }
        }
    }

    if (valueStack.length !== 1)
        throw new Error("Invalid RPN expression (too many/few operands).");

    return valueStack.pop();
}

// --- 7. Main Execution Flow ---

/**
 * Main calculation function.
 */
function calculate(expression) {
    initializeDefinitions();
    if (!expression || expression.trim().length === 0) {
        throw new Error("Expression cannot be empty.");
    }
    
    const tokens = tokenize(expression);
    const rpn = infixToRpn(tokens);
    
    // RPN DEBUG: console.log("RPN:", rpn); 
    
    return evaluateRpn(rpn);
}

// --- 8. Main CLI Loop (Node.js) ---

function main() {
    console.log("--- JavaScript CLI Calculator (Node.js Infix Mode) ---");
    console.log("Supported Operations:");
    console.log("  Binary: +, -, *, /, ^ (Power), r (Root: a r b = b-th root of a)");
    console.log("  Unary (Prefix): S, s, C, c, T, t, l, L (e.g., S30)");
    console.log("  Unary (Postfix): ! (Factorial, e.g., 6!)");
    console.log("  Constant: p (PI)");
    console.log("\nNOTE: Use explicit multiplication (e.g., 2*(3) is correct).");
    console.log("Type 'exit' or 'quit' to end.\n");

    const readline = require('readline').createInterface({
        input: process.stdin,
        output: process.stdout
    });

    const prompt = () => {
        readline.question("Expression: ", (input) => {
            const expression = input.trim();

            if (expression.length === 0 || expression.toLowerCase() === 'exit' || expression.toLowerCase() === 'quit') {
                readline.close();
                console.log("Exiting calculator. Goodbye!");
                return;
            }

            try {
                const result = calculate(expression);
                console.log(`Result: **${result.toFixed(10)}**\n`);
            } catch (e) {
                console.error(`Error: Invalid expression. ${e.message}\n`);
            }
            
            prompt(); // Loop back for the next expression
        });
    };

    prompt();
}

// Start the CLI application
main();