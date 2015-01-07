/*********************************************
* header for the helper                      *
*                                            *
* Created by: Brad Zacher                    *
* Computer Science Honours Project 2012      *
* Modified: 20/09/2012                       *
*********************************************/
#ifndef _GENOMEHELPER_H
#define _GENOMEHELPER_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>

#include "b_array.h"

#ifndef INT_MIN
	#define INT_MIN -32767
#endif
#ifndef INT_MAX
	#define INT_MAX 32767
#endif

#define true 1
#define false 0

#define MAX(A,B) (A > B ? A : B)
#define MIN(A,B) (A < B ? A : B)

int main(int, char **);

void testPhase3();

void testPhase1();
float compareChildren(int *, int *);

void testPhase2();

float AssignFitness();

int doMaths(int, int, char);
int distance(int, int);

int recurse(int);
int mrrc(int,int);
int * testCallValue(int);
int param(int, int);
int recurseHelper(int);

void treeData();

#endif
