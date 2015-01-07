#include "genomeHelper.h"

int * params;
int ** children;
int * output;
int num_fragments = 4;

void treeData() {
	params = (int*)b_allocate(sizeof(int), num_fragments);
	children = (int**)b_allocate(sizeof(int*), num_fragments);
	output = (int*)b_allocate(sizeof(int), num_fragments);

	int i = 0;

	params[i] = 4;
	children[i] = (int*)b_allocate(sizeof(int), 1);
	children[i][0] = 3;
	output[i++] = 24;

	params[i] = 3;
	children[i] = (int*)b_allocate(sizeof(int), 1);
	children[i][0] = 2;
	output[i++] = 6;

	params[i] = 2;
	children[i] = (int*)b_allocate(sizeof(int), 1);
	children[i][0] = 1;
	output[i++] = 2;

	params[i] = 1;
	children[i] = (int*)b_allocate(sizeof(int), 1);
	children[i][0] = 0;
	output[i++] = 1;

}
