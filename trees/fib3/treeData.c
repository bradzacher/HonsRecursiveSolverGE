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

	params[i] = 6;
	children[i] = (int*)b_allocate(sizeof(int), 3);
	children[i][0] = 3;
	children[i][1] = 4;
	children[i][2] = 5;
	output[i++] = 17;

	params[i] = 5;
	children[i] = (int*)b_allocate(sizeof(int), 3);
	children[i][0] = 2;
	children[i][1] = 3;
	children[i][2] = 4;
	output[i++] = 9;

	params[i] = 4;
	children[i] = (int*)b_allocate(sizeof(int), 3);
	children[i][0] = 1;
	children[i][1] = 2;
	children[i][2] = 3;
	output[i++] = 5;

	params[i] = 3;
	children[i] = (int*)b_allocate(sizeof(int), 3);
	children[i][0] = 0;
	children[i][1] = 1;
	children[i][2] = 2;
	output[i++] = 3;

}
