# compiler-tools
I'm still rapidly iterating this code base, so there's no stable API and a near complete lack of comments.

At the moment there's a lexer, and an indent based parse layer that builds a first draft token tree.
The decision to mandate the indent based tree structure was in order to prevent invalid code at some point in subtree from invalidating its parents and siblings.

The lexer uses a function first syntax for expressing languages expressible by finite state automata.
It uses lower case chars as char class identifiers, and upper case chars for expression substitions (with a recursion check on substitution registration).

Wild card - a predefined character class "." matches all supported characters (0 -> 0xff).
Kleen Operators - Currently no modifiers other than '1+' are supported.
  *(aA) - any number of (a character matching the 'a' class, followed by a String matching the 'A' class)
  *<1+>(aA) - one or more of (as above)

Or operator - the union
  +(A, B, C) - matches all strings in at least on language from A, B and C (whitespace is preserved in the expression tree, but trimmed before the tree is built).

Not operators
  !A - matches all Strings not in language A with regards to the 'universal' alphabet defined by char and String classes - i.e.: not just the alphabet of language A.
  ~a - matches all characters not in char set 'a', with regards the universal alphabet.
  
And operator - the intersection
  &(A, B) ≡ !&(!A, !B)

String literal
  '&(A, B)' - matches the string "&(A, B)", rather than the intersection of A and B

The machine that it builds is not strictly a finite state automata - it supports the naming of patterns, and the names are preserved across grouping operations.
At the moment only a full token match supports extracting a name
  - I intend to add support for extracting names of encapsulated substrings.
  - This won't always be unambiguous.

The name operator:
If we define 

x = ['0' ... '9', 'a' ... 'f']

d = ['0' ... '9']

H = @<hexadecimal>('0x'*<1+>x)
 
D = @<decimal>(*<1+>d)

then the language +(H, D) can extract tokens labeled as either hexadecimal or decimal.

Currently it still supports building Automata with ambiguous token forms
e.g.:

C = @<class-name>*<1+>(u*l)
 
S = @<const>(*<1+>u*('_'*<1+>u))

Both languages match *<1+>u.
I'm planning on implementing a feature where languages can be reverse extracted from Automata, so I can specify the ambiguity.

I already know how to do that on paper, so implementing it in code won't be an insurmountable task.

Right now I could detect the ambiguity - and state the languages that intersect, but not the language of the intersection.

The expression language is parsed using recursive descent with a single character look-ahead (it won't check multiple paths for matches).
Recursive descent is also going to be used to extract trees from series of tokens where nested brackets are present.

I have not yet decided whether or not to actually implement any automata for context free languages.

The current goal post is to get this to a state where it can transpile to java, with some simple support for collections (Sets, Maps and Lists), as well as arrays.

