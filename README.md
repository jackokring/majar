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

Errors are accumulated and only throw on a critical amount of them. This can
place the stack trace into the future, but maybe the errors show what happened bad
as it accumulates. A more logical reason is the algorithmic generation of
code by an AI, as it investigates a search space. It is better that more than
just the non-immediate fail space is searched as a recovered success is
perhaps more effective than a lesser search. The word `ok` clears errors. Consider
this to be some kind of genetic algorithm intron-extron mechanism improving
search space efficiency, and perhaps leading to correction reduction theorems.

The perfection of logic starts with a pedantic perfect logic machine. The
expanse of logic ends with a fast imperfect logic machine subject to insertion
replacement and all down tidy. Born of the clan of vision, creation was taken
seriously without a thought for the current best known. The future as they
said was everything. A dream was imperfect but through some relative effect
faster, and correctable with a great efficiency enough of the time.

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