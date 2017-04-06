Despector
===========

A java / kotlin decompilation tool and AST library.

The main goal of this library was to construct a complete Abstract Source Tree (AST) for a java
class file including nodes for all instructions. It is built of top of [ASM] which has AST
elements for methods but stops there and provides a simple opcode list.

While not as useful for modifying source and reconstruction a java class file this fill AST is
very useful for decompiling and for code analysis where you would like to search for patterns
on a statement rather than an opcode level.

For support and discussion see our [Development/Support Chat] on irc.esper.net in the #decompiler channel.

## Kotlin

At this time the kotlin support is quite new and all features are not set supported and bugs in the
output are to be expected.

# Usage as a Decompiler

`java -jar Despector.jar <--config=[path]> <--lang=[java|kotlin]> [sources...] [destination]`

- The `--config=` allows you to define a config file for certain decompilation settings.
- The `--lang=` forces the output to be in a particular language. Normal behaviour is to attempt to
determine the class files source language from its contents.

# Issues

This decompiler is still under heavy development and issues will happen. If you encounter any incorrect output
please open an issue in the [Issue Tracker]. At a minimum include the expected and encountered output. A compiled
class demonstrating the issue would also be extemely helpful.

# Feature Requests

Feature requests are always welcome and can be made in the [Issue Tracker] with as much information as possible.

# Configuration file

Here is a sample configuration file. It is optional but allows you to control decompilation settings
such as formatting. If you would like more settings open a feature request in the [Issue Tracker]. The
configuration file uses the [HOCON] configuration format.

```
# Despector decompiler configuration:

# Cleanup configuration
cleanup {
    # Cleanup operations to apply before emitting
    operations=[]
}
# Targeted cleanup operations
"cleanup_sections"=[]
# Emitter configuration
emitter {
    # Whether to emit synthetic members
    emit-synthetics=false
    # The path of the formatter configuration
    formatting-path="run/eclipse_formatter.xml"
    # One of: eclipse,intellij
    formatting-type=eclipse
    # The path of the import order configuration
    import-order-path="run/eclipse.importorder"
}
# Kotlin specific configuration
kotlin {
    # Whether to replace strings containing new lines with raw strings
    replace-multiline-strings=true
}
# Prints out opcodes of a method when it fails to decompile.
print-opcodes-on-error=true
```

[Gradle]: https://www.gradle.org/
[ASM]: http://asm.ow2.org/
[Development/Support Chat]: https://webchat.esper.net/?channels=decompiler
[Issue Tracker]: https://github.com/Despector/Despector/issues
[HOCON]: https://github.com/typesafehub/config/blob/master/HOCON.md
