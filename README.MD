# Pseudo

Pseudo is a programming language inspired by the specification outlined here: https://www.ocr.org.uk/Images/260952-pseudocode-guide.pdf.

It runs on the JVM, and you will soon be able to create a standalone jar from your source code. For now, Pseudo compiles to its own archive format (`.psa`), which must be executed using the Pseudo CLI.

### Building
In order to build Pseudo, simply clone the project, import it as a gradle project into your preferred IDE, run the `generateGrammarSource` task, and finally the `build` task .

There should now be an executable jar file in your `build/libs` directory .
### Usage
Here is a basic `helloworld.psc` program:
```
print("Hello, world!")
```
To run this program, we can do:

```
java -jar pseudo.jar compile helloworld.psc --run
```

The `compile` command will build a `helloworld.psa` archive, and the `--run` option tells Pseudo that we would also like to execute the program.

Precompiled archives can be executed using:
```
java -jar pseudo.jar run helloworld.psa
```

For more detailed examples, refer to the [examples folder](https://github.com/LlamaLad7/pseudo-kt/tree/master/examples) of this repository.

### Features
* If and switch statements (see [selection.psc](https://github.com/LlamaLad7/pseudo-kt/blob/master/examples/selection.psc))
* For, while, and do-until loops (see [loops.psc](https://github.com/LlamaLad7/pseudo-kt/blob/master/examples/loops.psc))
* Functions:
```
function triple(x)
    return x*3
endfunction

print(triple(12))
```
* Classes and inheritance (see [classes.psc](https://github.com/LlamaLad7/pseudo-kt/blob/master/examples/classes.psc))
* Arrays and lists (see [arrays.psc](https://github.com/LlamaLad7/pseudo-kt/blob/master/examples/arrays.psc) and [lists.psc](https://github.com/LlamaLad7/pseudo-kt/blob/master/examples/lists.psc))
* Console I/O:
```
print("Hello!")
name = input("What is your name? ")
```
* File I/O (see [files.psc](https://github.com/LlamaLad7/pseudo-kt/blob/master/examples/files.psc))
* Global variables:
```
global x = 10

function test
    print(x)
endfunction

test()
```
* Comments:
```
print("Hello!") // Greet the user
```
* A wide range of mathematical and logical operators (refer to [the specification](https://www.ocr.org.uk/Images/260952-pseudocode-guide.pdf))

### How it's made
Pseudo's parser and lexer are written in [ANTLR](https://www.antlr.org/), and the rest of the language is written in [Kotlin](https://kotlinlang.org/).

It compiles to Java Bytecode (see [JvmCompiler.kt](https://github.com/LlamaLad7/pseudo-kt/blob/master/src/main/kotlin/com/llamalad7/pseudo/compilation/JvmCompiler.kt)), with the help of ObjectWeb's bytecode generation framework [ASM](https://asm.ow2.io/) and half-cambodian-hacker-man's Kotlin DSL [Koffee](https://github.com/half-cambodian-hacker-man/Koffee).

### Final Words
If you would like to suggest a feature, or have found a bug, please don't hesitate to [open an issue](https://github.com/LlamaLad7/pseudo-kt/issues/new/choose).

If you use any code from this project it would be nice if you could give credit :D

Pseudo uses [BigDecimalUtil](https://github.com/javadev/calc/blob/master/src/main/java/com/github/calc/BigDecimalUtil.java) from javadev's "calc" for some mathematical operations, and it is included in this project as per the Apache 2.0 license.
