#include "genomeHelper.h"

int * params;
int ** children;
int * output;
int num_fragments = 5;

void treeData() {
	params = (int*)b_allocate(sizeof(int), num_fragments);
	children = (int**)b_allocate(sizeof(int*), num_fragments);
	output = (int*)b_allocate(sizeof(int), num_fragments);

	int i = 0;

	params[i] = 5;
	children[i] = (int*)b_allocate(sizeof(int), 2);
	children[i][0] = 3;
	children[i][1] = 4;
	output[i++] = 7;

	params[i] = 4;
	children[i] = (int*)b_allocate(sizeof(int), 2);
	children[i][0] = 2;
	children[i][1] = 3;
	output[i++] = 4;

	params[i] = 3;
	children[i] = (int*)b_allocate(sizeof(int), 2);
	children[i][0] = 1;
	children[i][1] = 2;
	output[i++] = 3;

	params[i] = 2;
	children[i] = (int*)b_allocate(sizeof(int), 0);
	output[i++] = 1;

	params[i] = 1;
	children[i] = (int*)b_allocate(sizeof(int), 0);
	output[i++] = 2;

}
