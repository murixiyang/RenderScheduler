#!/bin/bash

CLASSPATH="lib/*;src"

# Compile
javac -cp "$CLASSPATH" src/*.java

# Run Main
java -cp "$CLASSPATH" Main "$@"

# Keep the terminal window open
read -p "Press Enter to exit..."