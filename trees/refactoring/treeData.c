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

	params[i] = 12;
	children[i] = (int*)b_allocate(sizeof(int), 2);
	children[i][0] = 4;
	children[i][1] = 6;
	output[i++] = 4;

	params[i] = 6;
	children[i] = (int*)b_allocate(sizeof(int), 0);
	output[i++] = 2;

	params[i] = 24;
	children[i] = (int*)b_allocate(sizeof(int), 1);
	children[i][0] = 12;
	output[i++] = 7;

	params[i] = 36;
	children[i] = (int*)b_allocate(sizeof(int), 1);
	children[i][0] = 18;
	output[i++] = 9;

}
