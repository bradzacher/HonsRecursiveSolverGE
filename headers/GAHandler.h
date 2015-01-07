/*****************************************
* GA Handler Definition                  *
*                                        *
* Stores everything relating to the GA   *
*                                        *
* Created by: Brad Zacher                *
* Computer Science Honours Project 2012  *
* Modified: 20/09/2012                   *
*****************************************/
#ifndef _GAHANDLER_H
#define _GAHANDLER_H

#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <cmath>

/* the GALib and libGE libraries */
#include <ga/ga.h>
#include <GE/ge.h>
/* Genome definition */
#include "GEListGenome.h"

/* the time libraries */
#include <ctime>
#include <sys/timeb.h>

#include "SettingsHandler.h"
#include "ConnectionHandler.h"

using namespace std;

class GAHandler {
    public:
        /** VARS **/
        static GASteadyStateGA * ga;
        static GEGrammarSI mapper;
        static unsigned long int totalIndividualsRunTime;
        static unsigned long int totalIndividualsCompileTime;
        static long int totalIndividuals;
        static unsigned long int GAstartTime;
        static unsigned long int GAendTime;
        static ofstream stats;
        static int generationCounter;
    
        /** METHODS **/
        static void run();
    
        static void applySettings();
    
        static float objfunc(GAGenome &);
    
        static void initFuncSI(GAGenome &);
        static void initFuncRandom(GAGenome &);
    
        static void printStats(GASteadyStateGA *, GEGrammarSI);
        static void print_individual(const GAGenome &, GEGrammarSI);
        
        static vector<string> removeTokens(const string &);
    
        static unsigned long int ClockGetTime();
        static string convertMS(unsigned long int);

        static void prepareIndividual(FILE *, const string &);
        static string emptyFunctionCode();
        static string buildRecurseFunction(const string &);
        static string buildTestCallValueFunction(const string &);
        static string buildParamFunction(const string &);
        static string buildRecurseHelperFunction(const string &);

    private:
        static string return0;
        static string return1;
};

#endif
