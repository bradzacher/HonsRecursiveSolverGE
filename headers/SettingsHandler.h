/*****************************************
* Settings Handler Definition            *
*                                        *
* Stores everything relating to settings *
*                                        *
* Created by: Brad Zacher                *
* Computer Science Honours Project 2012  *
* Modified: 20/09/2012                   *
*****************************************/
#ifndef _SETTINGSHANDLER_H
#define _SETTINGSHANDLER_H

#define WRAPPER_LENGTH 500

#include <iostream>
#include <sstream>
#include <string>
#include "b_array_pp.h"

/* the GALib and libGE libraries */
#include <ga/ga.h>
#include <GE/ge.h>

using namespace std;

class SettingsHandler {
    public:
        /** VARS **/
        /* Buffers for start and end code of the individual.c file */
		static char Wrapper1[WRAPPER_LENGTH];
        static char Wrapper2[WRAPPER_LENGTH];
		static char Wrapper3[WRAPPER_LENGTH];
		static char Wrapper4[WRAPPER_LENGTH];
		static char Wrapper5[WRAPPER_LENGTH];
		static char paramFunction[WRAPPER_LENGTH];
        static struct geSettingsStruct geSettings;
        static GAParameterList gaParams;
        static string * alreadyFound;
        static int numAlreadyFound;
        static string treeDataObject;

        static bool verbose;

    
        /** METHODS **/
        static string loadGrammarFile(GEGrammarSI *);
        static void loadWrappers();

        static string compileTreeDataFile();
    
        static void parseGESettings();
        static int parseGEcmd(int, char **);
        
        static void parseGASettings();
        static void parseGAcmd(int, char **);

        static void println(string);
        static void println(bool, string);

        static void outputIndividual(const GAGenome &, GEGrammarSI);
        static string readOutputList();
        static bool checkPhenotype(const Phenotype);
};


/* Used to store the settings for libGE */
struct geSettingsStruct {
	unsigned int seed;              /* Random seed. */
	unsigned int wrappingEvents;	/* Wrapping events. */
	bool sensibleInit;              /* Use Sensible Initialisation. */
	unsigned int minSize;           /* Minimum size for Random Initialisation. */
	unsigned int maxSize;           /* Maximum size for Random Initialisation. */
	float grow;                     /* Grow rate for Sensible Initialisation. */
	unsigned int maxDepth;          /* Maximum tree depth for Sensible Initialisation. */
	unsigned int tailSize;          /* Tail size for Sensible Initialisation. */
	float tailRatio;                /* Tail ratio for Sensible Initialisation. */
	bool effectiveXO;               /* Use Effective Crossover? */
	string wrapperFolder;			/* folder and filename prefix for wrapper files */
	string grammarFile;     		/* Grammar file. */
	string treeDataFile;			/* Tree Data File. */
	string outputFile;				/* Output File. */
	string paramFile;				/* Parameter File. */
	int phase;						/* Phase Number */
	int distanceFormula;			/* Distance Formula Number */
    int maxTimeMS;                  /* The maximum runtime for an individual in milliseconds */
	string settingsFile;          	/* The GALib settings file. */
	string settingsFileGE;        	/* The libGE settings file. */
};

#endif
