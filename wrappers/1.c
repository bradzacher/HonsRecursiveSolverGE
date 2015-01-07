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

