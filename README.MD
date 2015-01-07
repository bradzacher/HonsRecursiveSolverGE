# From Pictures to Programs with Genetic Programming

Genetic programming (GP) is a powerful and widely used tool for evolving complex programs. In the context of GP, recursive programs have always been a difficult problem to tackle. Their compact and expressive form may seem like an ideal candidate for GP, although their characteristic structure causes standard evolutionary techniques to be unable to reliably produce results. This project presents a method to adapt GP (through the use of grammatical evolution) and inform the algorithm's fitness function using a number of small fragments of the recursive function's call tree.

For full details, please read the included PDF thesis.

###Build

To build, this application requires [GALib](http://lancet.mit.edu/ga/), as well as [libGE](http://bds.ul.ie/libGE/index.html).

- Run the included Makefile to build the C++ GE.
- Run the included Ant build script to build the Java GUI.

### Usage

Launch the GUI application using the included bash script.

The application can be entirely controlled using the GUI - trees can be assembled and processed.

Some example trees can be found within the trees folder.