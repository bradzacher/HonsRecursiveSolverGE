/*********************************************
* contains the functions called by to test a *
*      generated genome                      *
* A.K.A the REAL objective function          *
*                                            *
* Created by: Brad Zacher                    *
* Computer Science Honours Project 2012      *
* Modified: 20/09/2012                       *
*********************************************/

#include "genomeHelper.h"

extern int * params;
extern int ** children;
extern int * output;
extern int num_fragments;

float finalFitness;
int distFormula = 1;

/**
* MAIN METHOD
*/
int main(int argc, char *argv[]) {
	int phase = 0;
	if (argc > 1) {
		phase = argv[1][0] - '0';
	}
	if (argc > 2) {
		distFormula = argv[2][0] - '0';
	}

    //Initialize any required variables
    treeData();
    
    if (phase == 1) {
		// test the recursive call
    	testPhase1();
<<<<<<< .mine
    } else if (phase[0] == '2' || phase[0] == '3') { // phase 3 == (phase 2 with mrrc)
=======
    } else if (phase == 2 || phase == 3) { // phase 3 == (phase 2 with mrrc)
>>>>>>> .r758
    	// test the base case
    	testPhase2();
    }

    //calculate and print the fitness
    printf("%f\n", AssignFitness());

    return 0;
}

/**
* Tests the recursive parameter
*/
void testPhase1() {
	int i;

	float call_fitness = 0;

	/* start by testing the call values */
	float * fitnesses = (float*)malloc(sizeof(float) * num_fragments);

	/* calculate the fitness found from each fragment separately */
	for (i = 0; i < num_fragments; i++) {
		int p = params[i];
		int * c = children[i];
		
		int * result = testCallValue(p); // only dealing with functions that take 1 param for now..
		fitnesses[i] = compareChildren(c,result);
	}

	/* maybe we can do something with weighting here? */
	float M = num_fragments;
	for (i = 0; i < num_fragments; i++) {
		if (fitnesses[i] == -1) {
			 // if the fitness is -1, then the fragment had no children, so we don't count it
			M = M - 1.0; 
		} else {
			call_fitness += fitnesses[i];
		}
	}

	finalFitness = M / (call_fitness + M);
	//finalFitness = call_fitness / ((float)divisor);
}

/**
* Takes each element of comparison_children and tries to find a unique matching element in children
* @param children - the haystack to look through
* @param comparison_children - the needles to look for
* Note that the arrays are assume to have been assigned by b_allocate
*/
float compareChildren(int * children, int * comparison_children) {
	int i, j;

	// the penalty that an individual has received
	float penalty = INT_MAX;

	// try and match each of the comp_children with one of our children
	//int potential_matches = b_length(children);
	//int children_sz = potential_matches;
	int children_sz = b_length(children);
	int comparison_children_sz = b_length(comparison_children);

    if (children_sz == 0) {
        return -1;
    }
    
	// this vector marks elements as matched or not
	int * checked = (int*)malloc(sizeof(int) * children_sz);
	for (i = 0; i < children_sz; i++) {
		checked[i] = false;
	}

	for (i = 0; i < comparison_children_sz; i++) {
		if (comparison_children[i] == INT_MIN) { // something was wrong with this calculation, so skip it
			continue;
		}

		for (j = 0; j < children_sz; j++) {
			// if we've already matched this element, skip it
			if (checked[j] == true) {
				continue;
			}

			float theTest = distance(comparison_children[i], children[j]);
			if (theTest == 0) {
				checked[j] = true; // mark the element as matched
			}

			// penalize the function based on the smallest comparison value for each test
			penalty = MIN(theTest, penalty);
		}
	}

	return (penalty);
}

/**
* Run the individual for the input values taken from the
*    will run the function for all required test values
*/
void testPhase2() {
	int i;
	float potential_matches = num_fragments;

	float penalty = 0;
	for (i = 0; i < num_fragments; i++) {
		int result = recurse(params[i]);

		float diff = distance(result, output[i]);

		penalty += diff;
	}

	penalty = potential_matches / (penalty + potential_matches);

	finalFitness = (penalty);
}

/**
* Calculates the fitness
*/
float AssignFitness() {
    /* Sum the calculated fitness values and make sure they are bounded correctly */
    //finalFitness = 0.8 * inputOutputFitness + 0.2 * recursiveCaseFitness;
    finalFitness = MIN(MAX(finalFitness, 0), 1);
    
    return finalFitness;
}

/**
* Method to do maths..
*/
int doMaths(int a, int b, char c) {
	if (c == '*') {
        return a * b;
    } else if (c == '+') {
        //if ( (a != 0) && (b != 0) ) {
            return a + b;
        //}
    } else if (c == '-') {
        //if ( b != 0 ) {
            return a - b;
        //}
    } else if (c == '/') {
        if ( b != 0 ) {//&& b != 1 ) {
            return a / b;
        }
    } else if (c == '%') {
    	if ( b != 0 ) {
    		return a % b;
    	}
    }

    return INT_MIN;
}

/**
* Calculates the distance between two numbers
*/
int distance(int a, int b) {
	if (distFormula == 0) { // all or nothing
		return (a != b);
	} else if (distFormula == 1) { // standard distance
		return abs(a - b);
	} else if (distFormula == 2) { // squared distance
		int x = abs(a - b);
		return x*x;
	} else {
		return -1;
	}
}
