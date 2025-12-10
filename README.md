# CalQl8r

My Calculate library implementation in multiple languages

![Static Badge](https://img.shields.io/badge/m2kdevelopments-purple?style=plastic&logo=github&logoColor=purple&label=developer&link=https%3A%2F%2Fgithub.com%2Fm2kdevelopments)
![Static Badge](https://img.shields.io/badge/MIT-green?style=plastic&logo=license&logoColor=green&label=license)
![Static Badge](https://img.shields.io/badge/buy_me_a_coffee-yellow?style=plastic&logo=buymeacoffee&logoColor=yellow&label=support&link=https%3A%2F%2Fwww.buymeacoffee.com%2Fm2kdevelopments)
![Static Badge](https://img.shields.io/badge/paypal-blue?style=plastic&logo=paypal&logoColor=blue&label=support&link=https%3A%2F%2Fpaypal.me%2Fm2kdevelopment)


## Using Single Letter Values to Represent Functions
### Trigonometry
<ul>
    <li>Sin = S</li>
    <li>Sinh = s</li>
    <li>Cos = C</li>
    <li>Cosh = c</li>
    <li>Tan = T</li>
    <li>Tanh = t</li>
</ul>

### Logarithmns and Exponentials
<ul>
    <li>e = e</li>
    <li>ln = E</li>
    <li>log10 = L</li>
    <li>alogb = l</li>
</ul>

### Constants
<ul>
    <li>PI = p</il>
</ul>


### Others Functions
<ul>
    <li>Square Root = 2r</li>
    <li>Cube Root = 3r</li>
    <li>Root = r</li>
    <li>Pol = P</li>
    <li>Rec = R</li>
</ul>



## Run it with Node JS
```bash
# https://nodejs.org/en/download
node src/main/main.js "1+1"
```


## Run it with Java 
```bash
javac src/main/Main.java 
java src/main/Main "1+1"
```

## Run it with Kotlin
```bash
kotlinc -version
kotlinc src/main/main.kt
kotlin src/main/MainKt "1+1"
```

## Run it with C (gcc)
```bash
# Compile and run
gcc src/main/main.c -lm -o src/main/calql8r
./src/main/calql8r "1465+225+55.7 36 63-9+8* 9 /8 + 2^2 + 2r4 + p + (1+1 + (2r4) + 3) + 6!+789"
```

## Run it with Dart
```bash
# Just Run
dart src/main/main.dart 1 + 1
```
```bash
# Compile and run
dart compile exe src/main/main.dart -o src/main/calql8r
./src/main/calql8r "1+1"
```


## Run it with C#
```bash
# dotnet version 10 or above
# https://dotnet.microsoft.com/en-us/download/dotnet/10.0
dotnet run src/main/Main.cs 1+1
```

## Run it with Python
```bash
# Python v3 and above
# https://www.python.org/downloads/
python src/main/main.py 1 + 1
```

## Method of Approach for C
I was using a ton of pointers in my initial implementation. Mallocing and Free memory like a boss but I kept getting <b>Segmentation fault</b> errors. Skill issues I know. So I'm using a different approach. I'll have an <i>arena</i> of memory that I will malloc once when the program starts and have a index to point to the end of the arena as the calculations go. Then free the entire arean of memory when the calculation is complete.



## Support
You can support us with any amount. It's all appreciated.

<a href="https://www.buymeacoffee.com/m2kdevelopments" target="_blank">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" />
</a>

<a href="https://paypal.me/m2kdevelopment" target="_blank">
    <img src="https://www.paypalobjects.com/webstatic/mktg/logo/pp_cc_mark_111x69.jpg" alt="PayPal Logo" />
</a>
