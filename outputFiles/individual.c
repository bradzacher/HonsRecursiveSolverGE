#include "genomeHelper.h"

extern struct timeval tv;
extern double startTime;
extern double maxTime;

int limiter;
int limiter_max;

int recurse(int x) {
    gettimeofday(&tv, NULL);
    double currTime = (tv.tv_sec) * 1000 + (tv.tv_usec) / 1000;
    if (currTime - startTime > maxTime) {
        printf("0\n");
        exit(0);
    }
    //generated code starts here

	limiter = 0;
	limiter_max = x*x;
	return mrrc(x,2);
}

int mrrc(int x, int c) {
	if ( (limiter++) > limiter_max ) {
		return -1;
	}
	//generated code starts here
#define op * 
#define base_op_val 1
int i, result;

if (x < 2) {
 return x;
}

result = base_op_val;
for (i = c; i < (x - 2); i++) {
 if ((doMaths(x, i, '%') == 0)) {
 result = result op mrrc(doMaths(x, i, '/'), i);
 }
}

return result;


    //generated code ends here
}

int * testCallValue(int x) {
    //generated code starts here

return 0;

    //generated code ends here
}

int param(int x, int num) {
    //generated code starts here

return 0;

    //generated code ends here
}

int recurseHelper(int x) {
    //generated code starts here

return 0;

    //generated code ends here
}
