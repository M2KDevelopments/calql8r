const readline = require("readline");

// --- Factorial ---
function factorial(n) {
    if (n < 0) throw "Negative factorial";
    let r = 1;
    for (let i = 2; i <= n; i++) r *= i;
    return r;
}

// --- Preprocess custom syntax ---
function preprocess(expr) {
    expr = expr.replace(/\s+/g, "");

    // Replace pi constant
    expr = expr.replace(/p/g, Math.PI.toString());

    // Root operator 2r4 → (4 ** (1/2))
    expr = expr.replace(/(\d+)r(\d+)/g, "( $2 ** (1/$1) )");

    // Factorial n! → factorial(n)
    expr = expr.replace(/(\d+)!/g, "factorial($1)");

    // Trig functions
    expr = expr.replace(/\bS\(/g, "Math.sin(");
    expr = expr.replace(/\bs\(/g, "Math.sinh(");
    expr = expr.replace(/\bC\(/g, "Math.cos(");
    expr = expr.replace(/\bc\(/g, "Math.cosh(");
    expr = expr.replace(/\bT\(/g, "Math.tan(");
    expr = expr.replace(/\bh\(/g, "Math.tanh(");

    // Logs
    expr = expr.replace(/\bl\(/g, "Math.log(");       // natural log
    expr = expr.replace(/\bL\(/g, "Math.log10(");     // log10

    // Power ^ → **
    expr = expr.replace(/\^/g, "**");

    return expr;
}

// --- CLI Loop ---
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

console.log("JavaScript CLI Scientific Calculator (type 'exit' to quit)");

function loop() {
    rl.question("> ", (input) => {
        if (input.toLowerCase() === "exit") {
            rl.close();
            return;
        }

        try {
            const processed = preprocess(input);

            const result = eval(processed);
            console.log("= " + result);
        } catch (err) {
            console.log("Error:", err.toString());
        }

        loop();
    });
}

loop();
