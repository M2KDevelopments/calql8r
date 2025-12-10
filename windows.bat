REM Compile Step
echo Main-Class: src.main.Main> src/main/MANIFEST.MF
javac src/main/Main.java
jar cvfm src/main/Main.jar src/main/MANIFEST.MF src/main/*.class

kotlinc src/main/Main.kt -include-runtime -d src/main/CalQl8r.jar


REM Run Step
node src/main/main.js 1+1 >> answers.txt

go run src/main/main.go 1+1 >> answers.txt

dotnet run src/main/Main.cs 1+1 >> answers.txt

dart src/main/main.dart 1+1 >> answers.txt

python src/main/main.py 1+1 >> answers.txt

CMD /C "cd src/main/ && main.exe 1+1 >> ../../answers.txt"

java -jar src/main/CalQl8r.jar 1+1 >> answers.txt