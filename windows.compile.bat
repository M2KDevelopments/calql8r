echo Main-Class: src.main.Main> src/main/MANIFEST.MF
javac src/main/Main.java
jar cvfm src/main/Main.jar src/main/MANIFEST.MF src/main/*.class

kotlinc src/main/Main.kt -include-runtime -d src/main/CalQl8r.jar

