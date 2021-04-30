majar
=
Welcome to the source git for `majar` a Java package covering some of my 
development.

Project Scope
-
* A simple DSL interpreter with late binding, and a simple parser.
* Compression codecs.
* Some website tools.
* Some AI algorithms.
* A database.

Language Style
-
The language `majar` has a simple single token read ahead parser, and most
of the complexity comes from logical concepts such as not all `true` values are equal
while the `false` value has a null execution contract. Built around the word
`book` to sort out execution `context` and hash tables via `safe`. It includes
coding ease features like `space` to make new stacks and remember old ones, along
with `time` to make threads. Multi-threading was a big part of the goal.

The logic bit could be confusing as `false` is actually true, until it evaluates
to null and is then false. The word `elucidate` turns null back into `false` and
anything else into `true` and so always returns something true, but useful for
printing with `print` though.

Influences
-
* `forth` but with late binding and some nicer data structuring.
* `cgi-bin` for all its spots.
* `j/apl` but it's not a replacement.
* `lisp` but it doesn't use lists directly.
* `lua` a nice little gem.
* `java` an easy workhorse to do the backend connection to JNI C.
* `algol/pascal` it was good. Everything from the triple ref to the structure.

Dedications
-
* A special thanks to Ohm who actually has a paid up licence for the `Kodek` when complete.
* All the 90s special raved up parties people and DJs.

Secrets and .gitignore
-
Anything commercially sensitive will load via an exception loader and perhaps not
appear in the repo. You could always inquire if you want a better look.

Funding and Openness
-
I don't mind if I do get some money. Sounds good.