# Java with less boilerplate

Imagine Java or C# with more compact Ruby-like syntaxis: say hello to Knish! 
Knish is statically typed, but at the same time it does not reuqire to write 
types of variables and methods explicitly. For example,
```dart
class Knish {
  static print(object) {
    System.print("Knish says: ");
    System.print(object);
  }
}

Knish.print(1);
Knish.print("2");
```
is a valid Knish program. However, 
```dart
class Knish {
  static print(object) {
    System.print("Knish says: ");
    System.print(object + 1);
  }
}

Knish.print(0);
Knish.print("1");
```
is not since the argument of ``Knish.print`` in ``Knish.print("1")`` has wrong type 
(you can add only strings to a string).

It is important to note that jKnish is a prototype; hence, it is not efficient in almost any 
way: the parser and lexer are handwritten, the definite assignement makes a lot of unnecessary
copies of bitsets, the type checker has cubic worst-case running time, and, the most important, 
the interpreter is a tree-walking algorithm so it is very slow.

# Syntax 
Knish's syntax is a mix of Ruby and Javascript; however, it is a bit streamlined.

## Comments

Line comments start with // and end at the end of the line:
```dart
// This is a comment.
```

## Identifiers

Naming rules are similar to other programming languages. Identifiers start
with a letter or underscore and may contain letters, digits, and underscores. 
Case is sensitive.
```dart
hi
camelCase
PascalCase
_under_score
abc123
ALL_CAPS
```

## Blocks
Knish uses curly braces to define blocks. You can use a block anywhere a
statement is allowed, like in control flow statements. Method and function
bodies are also blocks. For example, here we have a block for the then case,
and a single statement for the else:

```dart
if (happy and knowIt) {
  hands.clap()
} else System.print("sad")
```
