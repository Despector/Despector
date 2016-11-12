Despector
===========

A java decompilation tool and AST library.

The main goal of this library was to construct a complete Abstract Source Tree (AST) for a java
class file including nodes for all instructions. It is built of top of [ASM] which has AST
elements for methods but stops there and provides a simple opcode list.

While not as useful for modifying source and reconstruction a java class file this fill AST is
very useful for decompiling and for code analysis where you would like to search for patterns
on a statement rather than an opcode level.

# Usage as a Decompiler

`java -jar Despector.jar [sources...] [destination]`

Command line options and/or configuration is limited at the moment but is planned.
