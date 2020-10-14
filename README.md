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
Knish's syntax is very similar to Wren's syntax; i.e., it is a mix of Ruby and Javascript.

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

## Literals

Knish supports literlas for Boolean values, integers, strings, and nil.
```dart
true
false
1
2
3
"hello!"
nil
```

## Method Calls
Knish is an object oriented language, so most code consists of invoking methods
on objects; e.g.,
```dart
System.print("Hi!")
```
You have a receiver expression (here ``System``) followed by a ``.``, and 
then a name (``print``) and an argument list in parentheses (``("Hi!")``). 
Multiple arguments are separated by commas:
```dart
list.insert(3, "item")
```
The argument list can also be empty:
```dart
list.clear()
```

### Signature
Unlike the majority of dynamically-typed languages, in Knish a class can have 
multiple methods with the same name, as long as they have different signatures.
The signature includes the method’s name along with the number of arguments 
it takes. In technical terms, this means you can overload by arity.

### Getters
Some methods exist to expose a stored or computed property of an object. 
These are getters and have no parentheses:
```dart
"string".count
-12.abs
```
A getter is not the same as a method with an empty argument list. The ``()``
is part of the signature, so count and count() have different signatures. 
Unlike Ruby’s optional parentheses, Knish wants to make sure you call a getter
like a getter and a ``()`` method like a ``()`` method. These don’t work:
```dart
"string".count()
```
If you’re defining some member that doesn’t need any parameters, you need to
decide if it should be a getter or a method with an empty () parameter list. 
The general rules are:

- If it modifies the object or has some other side effect, make it a method:
  ``list.clear()``
- If the method supports multiple arities, make the zero-parameter case a 
  ``()`` method to be consistent with the other versions:
  ``System.print()`` and ``System.print("Hi!")``
- Otherwise, it can probably be a getter.

### Setters

A getter lets an object expose a public “property” that you can read. Likewise, 
a setter lets you write to a property:

```dart
person.height = 178
```
This is a syntactic sugar for method call; from the language’s perspective,
the above line is just a call to the ``height=(_)`` method on person, passing in 
``178``.

Since the ``=(_)`` is in a part of the setter’s signature, an object can have both
a getter and setter with the same name without a collision. Defining both lets you
provide a read/write property.

### Operators
Knish has most of the standard operators from other langiages:
```dart
! -
```
They are just method calls on their operand without any other arguments. An expression
like ``!possible`` means "call the ! method on possible“.
```dart
* / + - < <= > >= == !=
```
Like prefix operators, binary operators are all funny ways of writing method calls.
The left operand is the receiver, and the right operand gets passed to it. So ``a + b`` 
is semantically interpreted as “call the +(_) method on a, passing it b“.

Note that ``-`` is both a prefix and an infix operator. Since they have different 
signatures (``-`` and ``-(_)``), there’s no ambiguity between them.
