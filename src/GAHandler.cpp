/*****************************************
* GA Handler Function Defs               *
* Created by: Brad Zacher                *
* Computer Science Honours Project 2012  *
* Modified: 20/09/2012                   *
*****************************************/
#ifndef _GAHANDLER_CPP
#define _GAHANDLER_CPP

#include "GAHandler.h"

/**
* Variable Declarations
*/
GASteadyStateGA * GAHandler::ga = NULL;
GEGrammarSI GAHandler::mapper;
unsigned long int GAHandler::totalIndividualsRunTime = 0;
unsigned long int GAHandler::totalIndividualsCompileTime = 0;
long int GAHandler::totalIndividuals = 0;
unsigned long int GAHandler::GAstartTime = 0;
unsigned long int GAHandler::GAendTime = 0;
ofstream GAHandler::stats("outputFiles/stats");
int GAHandler::generationCounter = 1;

string GAHandler::return0 = "return 0;\n";
string GAHandler::return1 = "return 1;\n";

extern bool recursiveTestPhase;

/**
*
*/
void GAHandler::run() {
    /* An informative header */
	SettingsHandler::println("");
    cout << "Running Evolutionary Cycle:";
    if (!SettingsHandler::verbose) {
    	cout << endl;
    }
    SettingsHandler::println(string(" (note \".\" = 1, \":\" = 10, \";\" = 50 and \"|\" = 100 generations passed)"));

    /* Grab the current time */
    GAHandler::GAstartTime = GAHandler::ClockGetTime();

    /* Apply all settings to ga, including random seed - this actually does one evaluation */
    GAHandler::ga->initialize(SettingsHandler::geSettings.seed);
    
    /* Run The GA */
    while(!GAHandler::ga->done()) {
        GAHandler::ga->step();
        
        /* Send the stats to the grapher if connected */
        if (ConnectionHandler::isConnected()) {
            ConnectionHandler::streamStats();
        }
        
        /* makes it easier to guess from the console what generation the system is up to */
        if (GAHandler::generationCounter % 100 == 0) {
            cout << "|" << endl;
        } else if (GAHandler::generationCounter % 50 == 0) {
            cout << ";";
        } else if (GAHandler::generationCounter % 10 == 0) {
            cout << ":";
        } else {
            cout << ".";
        }
        cout.flush();
        
        /* Write stats to files */
        GAHandler::stats << GAHandler::ga->statistics().generation() << "\t"
                         << GAHandler::totalIndividuals << "\t"
                         << GAHandler::ga->statistics().current(GAStatistics::Maximum) << "\t"
                         << GAHandler::ga->statistics().current(GAStatistics::Mean) << "\t"
                         << GAHandler::ga->statistics().current(GAStatistics::Minimum) << "\n";
        GAHandler::generationCounter++;

        if ((GAHandler::ga->statistics().bestIndividual().score() == 1) && (GAHandler::generationCounter > 5)) {
        	break;
        }
    }
    cout << endl;
    
    /* Grab the current time */
    GAHandler::GAendTime = GAHandler::ClockGetTime();
}

/**
* Applies all of the settings from the SettingsHandler
* NOTE THIS WILL OVERWRITE ALL DATA IN THE ga OBJECT
*/
void GAHandler::applySettings() {
    /* Create genome, tell it to use our objective function */
    GEListGenome genome;
    genome.evaluator(objfunc);
    
    /* Use our initialisation function (initFuncRandom or initFuncSI). */
    if (!SettingsHandler::geSettings.sensibleInit) {
    	SettingsHandler::println(string("\tUsing random initialisation."));
        genome.initializer(initFuncRandom);
    } else {
    	SettingsHandler::println(string("\tUsing sensible initialisation."));
        genome.initializer(initFuncSI);
    }
    
    /* Use the one-point xo from GALib, or the libGE effective version. */
    if (!SettingsHandler::geSettings.effectiveXO) {
    	SettingsHandler::println(string("\tUsing one point crossover."));
        genome.crossover(GEListGenome::OnePointCrossover);
    } else {
    	SettingsHandler::println(string("\tUsing effective crossover."));
        genome.crossover(GEListGenome::effCrossover);
    }
    
    /* Use the libGE point-mutation. */
    genome.mutator(GEListGenome::pointMutator);

    /* Create GA with a steady-state approach. */
    SettingsHandler::println(string("\tSetting up GALib Steady State System."));
    ga = new GASteadyStateGA(genome);
    
    /* Associate parameters. */
    ga->parameters(SettingsHandler::gaParams);

    /* Select which scores we should track */
    ga->set(gaNselectScores, GAStatistics::AllScores);
    
    /* Mapper settings. */
    SettingsHandler::println(string("\tSetting up genome mapper."));
    mapper.setGenotypeMaxCodonValue(UCHAR_MAX);
    mapper.setPopSize(ga->populationSize());
    mapper.setGrow(SettingsHandler::geSettings.grow);
    mapper.setMaxDepth(SettingsHandler::geSettings.maxDepth);
    if (SettingsHandler::geSettings.tailSize) {
        mapper.setTailSize(SettingsHandler::geSettings.tailSize);
    } else {
        mapper.setTailRatio(SettingsHandler::geSettings.tailRatio);
    }
}

/**
* The Objective function itself!
* @param g - the genome to process
* @return the fitness value
*/
float GAHandler::objfunc(GAGenome &g) {
    GEListGenome &genome = static_cast<GEListGenome&>(g);
    
    unsigned long int startCompileTime;
    unsigned long int stopCompileTime;
    unsigned long int startRunTime;
    unsigned long int stopRunTime;
    
    /* Assign genotype to mapper */
    mapper.setGenotype(genome);
    
    /* Grab phenotype */
    Phenotype const * phenotype=mapper.getPhenotype();
    
    if(phenotype->getValid()) {
    	/* if this phenotype is in the already found list, ignore it */
    	if (SettingsHandler::checkPhenotype(*phenotype)) {
            //cout << *phenotype << " (" << 0.0 << ")\n";
    		return 0.0;
    	}

        FILE *file;
        float fitness;
        
        /* Create output file */
        if( !(file = fopen("outputFiles/individual.c", "w")) ) {
        	SettingsHandler::println(true, string("Could not open outputFiles/individual.c."));
        	SettingsHandler::println(true, string("Execution aborted."));
            exit(0);
        }

		/* Prepares the individual c code file */
        GAHandler::prepareIndividual(file, phenotype->getString());

    	fclose(file);

        //Compile and execute file with GCC
        /* the below system command:
            1) compiles the newly created code file with the evaluation function
                - "-pipe" is used so that the system doesn't create temporary files (i.e. saves time as there are no unneeded reads/writes)
            2) executes the program (which evaluates the fitness of the created function)
            3) pipes the output from the evaluation to a file
        */
        /* NOTE - the pipe 2>&1 pipes the stderr to stdout so that individuals that somehow segfault don't print an ugly message to the main output */
        ostringstream cmd1;
        ostringstream cmd2;
        cmd1 << "tcc -o outputFiles/individual -pipe outputFiles/individual.c " << SettingsHandler::treeDataObject << " obj/genomeHelper.o obj/b_array.o -w -Iheaders 2>&1";
        cmd2 << "./outputFiles/individual " << SettingsHandler::geSettings.phase << " " << SettingsHandler::geSettings.distanceFormula << " 10 > outputFiles/result 2>&1";

        startCompileTime = ClockGetTime();
        if( system(cmd1.str().c_str()) != 0) {
        	//cerr << cmd1.str() << endl;
        	SettingsHandler::println(true, string("Compilation failed, aborting. "));
            exit(0);
        }
        stopCompileTime = ClockGetTime();
        startRunTime = ClockGetTime();
        if( system(cmd2.str().c_str()) != 0) {
        	//cerr << cmd1.str() << endl;
        	//cerr << cmd2.str() << endl;

        	ostringstream s;
        	s << "Execution failed: \"";
            /* Open result file, containing fitness score */
            if( (file = fopen("outputFiles/result", "r")) ) {
            	char buff[50];
            	if (!fgets(buff, 50, file)) {
            		s << "unknown reason";
            	} else {
            		if (buff[strlen(buff)-1] == '\n') {
            			buff[strlen(buff)-1] = '\0';
            		}
            		s << buff;
            	}
            }
            //s << "\", aborting. "; 
            SettingsHandler::println(true, s.str());
            fclose(file);
            //exit(0);
        }
        stopRunTime = ClockGetTime();
        totalIndividualsRunTime += (stopRunTime - startRunTime);
        totalIndividualsCompileTime += (stopCompileTime - startCompileTime);
        totalIndividuals++;
        
        /* Open result file, containing fitness score */
        if( !(file = fopen("outputFiles/result", "r")) ) {
        	SettingsHandler::println(true, string("Could not open result file."));
        	SettingsHandler::println(true, string("Execution aborted."));
            exit(0);
        }

        if ( fscanf(file, "%f", &fitness) <= 0 ) {
            //cerr << *phenotype << endl;
            //cerr << "Fitness file was empty or unable to be read.\n";
            //cerr << "Ignoring...\n";
            fitness = 0;
        }
        fclose(file);

        if(isinf(fitness) || isnan(fitness) || !isfinite(fitness)) {
            fitness = 0;
        }
        
        //cout << " (" << fitness << ")\n";
        //cout << *phenotype << " (" << fitness << ")\n";

        /* Set effective size of genome */
        genome.setEffectiveSize(mapper.getGenotype()->getEffectiveSize());

        return fitness;
    } else {
        //cout << *phenotype << endl;
        return 0;
    }
}

/**
* Randomly initialise genome with minSize<=length<=maxSize genes, of 0<=value<=255.
* @param g - the genome to initialise
*/
void GAHandler::initFuncRandom(GAGenome &g){
	GAListGenome<unsigned char> &genome = static_cast<GAListGenome<unsigned char> &>(g);

	//Destroy any pre-existing list
	while(genome.head()) {
        genome.destroy();
    }

	//Create all individuals with a length between minSize and maxSize.
	int n = GARandomInt(SettingsHandler::geSettings.minSize, SettingsHandler::geSettings.maxSize);
	//Each gene is a number between 0 and 255
	//Insert the first gene
	genome.insert(GARandomInt(0, static_cast<unsigned char>(-1)), GAListBASE::HEAD);
	//Create rest of list
	for(int ii = 0; ii < n-1; ii++) {
		genome.insert(GARandomInt(0,static_cast<unsigned char>(-1)));
    }
}

/**
* Sensibly initialise genome with minSize<=length<=maxSize genes
* @param g - the genome to initialise
*/
void GAHandler::initFuncSI(GAGenome &g){
	GAListGenome<unsigned char> &genome = static_cast<GAListGenome<unsigned char> &>(g);

	//Destroy any pre-existing list
	while(genome.head()) {
        genome.destroy();
    }

	// Apply sensible initialisation to genotype
	if(!mapper.init()) {
		SettingsHandler::println(true, string("Error using sensible initialisation."));
		SettingsHandler::println(true, string("Execution aborted."));
		exit(0);
	}
    
	// Now copy genotype onto genome
	Genotype::const_iterator genIt = (mapper.getGenotype())->begin();
	
    // Insert the first gene
	genome.insert(*(genIt++),GAListBASE::HEAD);
	
    //Create rest of list
	while(genIt != (mapper.getGenotype())->end()) {
		genome.insert(*genIt);
		genIt++;
	}
}

/**
* Prints the stats nicely to stdout
* @param ga - the object to pull the stats from
* @param totalIndividualsRunTime - the total time taken to score individuals
* @param totalIndividuals - the total number of individuals processed
* @param startTime - the time that the main method began execution
* @param GAstartTime - the time that the set up finished and the GA began executing
* @param GAendTime - the time that the GA finished executing
* @param mapper - the mapper to use to map to a phenotype
*/
void GAHandler::printStats(GASteadyStateGA * ga, GEGrammarSI mapper) {
	SettingsHandler::println(string("Statistics:"));
	ostringstream s;
	s << ga->statistics();
	SettingsHandler::println(s.str());
    
    /* Print best individual */
    print_individual(ga->statistics().bestIndividual(), mapper);
    
    /* Print the total runtimes */
    cout << endl;
	stringstream s1;
	s1 << "Average time to compile each genetic individual " << convertMS(totalIndividualsCompileTime / totalIndividuals) << ".";
	SettingsHandler::println(s1.str());
	cout << "Average time to test each genetic individual " << convertMS(totalIndividualsRunTime / totalIndividuals) << "." << endl;
	stringstream s2;
	s2 << "Total number of individuals tested " << totalIndividuals << ".";
	SettingsHandler::println(s2.str());
    cout << "Total time taken to run the GA " << convertMS(GAendTime - GAstartTime) << "." << endl;
    cout << "Total number of generations: " << (GAHandler::generationCounter-1) << " generations" << endl;

    /* Write the result to a file */
    SettingsHandler::outputIndividual(ga->statistics().bestIndividual(), mapper);
}

/**
* Print an individual nicely to stdout
* @param g - the genome to map and print
* @param mapper - the mapper to use to map the genome
*/
void GAHandler::print_individual(const GAGenome &g, GEGrammarSI mapper) {
    GAListGenome<unsigned char> &genome = (GAListGenome<unsigned char> &) g;
    
    /* Assign genotype to mapper */
    mapper.setGenotype(genome);
    
    /* Print phenotype */
    cout << "Best individual:";
	if (!SettingsHandler::verbose) {
		cout << "(Fitness Score = " << g.score() << ")";
	}
	cout << endl;
    cout << *(mapper.getPhenotype()) << endl;
    if (SettingsHandler::verbose) {
		stringstream s1;
		s1 << "Genotype         = " << *mapper.getGenotype();
    	SettingsHandler::println(s1.str());
		stringstream s2;
		s2 << "Total length     = " << mapper.getGenotype()->size();
    	SettingsHandler::println(s2.str());
		stringstream s3;
		s3 << "Effective length = " << mapper.getGenotype()->getEffectiveSize();
    	SettingsHandler::println(s3.str());
        stringstream s;
        s << "Wrapping events = " << mapper.getGenotype()->getWraps();
        SettingsHandler::println(s.str());
    }
}

/**
* Takes in the individual, tokenizes out the recursive calls and stores them in a vector
* i.e. if the recursive case is "return recurse(1 + x) + recurse(x*x);", then the elements of the vector will be "1 + x" and "x*x"
* @param s - the phenotype (as a string) to use
* @return a vector<string> containing the recursive calls
*/
vector<string> GAHandler::removeTokens(const string &s) {
    vector<string> elems;
    string delim = "/**/";
    
    int len = s.length();
    int i = 0;
    
    int start = 0;
    int end = 0;
    
    int breakVal = int(string::npos);
    
    while(i < len) {
        start = s.find(delim, i) + delim.length();
        if ( (start != breakVal) && (start > i) ) {
            end = s.find(delim, start);
        } else {
            break;
        }
        if (end == breakVal) {
            break;
        }
        
        int subStrLen = end - start;

        elems.push_back( s.substr(start, subStrLen) );
        
        i = end + 1;
    }
    
    return elems;
}

/**
* Gets the current clock time
* @return the current time in milliseconds
*/
unsigned long int GAHandler::ClockGetTime() {
    timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts); /* Get the time */
    return ts.tv_sec * 1000L + ts.tv_nsec / 1000000L; /* Convert it to miliseconds */
}

/**
* Converts an unsigned long int representing a time in milliseconds to a time string dependent on how big it is (milliseconds, seconds, minutes or hours)
* @param time - the time to convert
* @return a string representing the time ("XX [hours/minutes/seconds/milliseconds]")
*/
string GAHandler::convertMS(unsigned long int time) {
    ostringstream str;
    
    if (time > 3600000) {       /* convert to hours */
        str << (((double)time) / 3600000.0) << " hours";
    } else if (time > 120000) { /* convert to minutes */
        str << (((double)time) / 60000.0) << " minutes";
    } else if (time > 2000) {   /* convert to seconds */
        str << (((double)time) / 1000.0) << " seconds";
    } else {                    /* leave as milliseconds */
        str << time << " milliseconds";
    }
    
    return str.str();
}

/**
* Writes all the code to the individual's c file
* @param file - the pointer to the file to write to
* @param phenotype - the individual to prepare and write out
*/
void GAHandler::prepareIndividual(FILE * file, const string &s) {

	/* Write 1st buffer to file */
	fprintf(file, "%s", SettingsHandler::Wrapper1);

//int recurse(int x) {
	fprintf(file, "%s", buildRecurseFunction(s).c_str());
//}

	/* Write 2nd buffer to file */
	fprintf(file, "%s", SettingsHandler::Wrapper2);

//int testCallValue(int x) {
	fprintf(file, "%s", buildTestCallValueFunction(s).c_str());
//}

	/* Write 3rd buffer to file */
	fprintf(file, "%s", SettingsHandler::Wrapper3);

//int param(int x, int num) {
	fprintf(file, "%s", buildParamFunction(s).c_str());
//}

	/* Write 4th buffer to file */
	fprintf(file, "%s", SettingsHandler::Wrapper4);

//int recurseHelper(int x, int depth) {
	fprintf(file, "%s", buildRecurseHelperFunction(s).c_str());
//}

	/* Write 5th buffer to file */
	fprintf(file, "%s", SettingsHandler::Wrapper5);
}

/**
* The default empty function code
* @return the code to put into the function
*/

string GAHandler::emptyFunctionCode() {
	return return0;
}

/**
* builds the code for the "int recurse(int x)" function
* @param s - the individual to construct from
* @return the code to put into the function
*/
string times_op = string("*");
string divide_op = string("/");
string plus_op = string("+");
string minus_op = string("-");
string GAHandler::buildRecurseFunction(const string &s) {
	if (SettingsHandler::geSettings.phase == 2){
		return s;
	} else if (SettingsHandler::geSettings.phase == 3) {
		ostringstream retVal;

		retVal << "\t" << "limiter = 0;" << endl;
		retVal << "\t" << "limiter_max = x*x;" << endl;
		retVal << "\t" << "return mrrc(x,2);" << endl;
		retVal << "}" << endl;
		retVal << endl;
		retVal << "int mrrc(int x, int c) {" << endl;
		retVal << "\t" << "if ( (limiter++) > limiter_max ) {" << endl;
		retVal << "\t\t" << "return -1;" << endl;
		retVal << "\t" << "}" << endl;
	    retVal << "\t//generated code starts here" << endl;
		retVal << s << endl;

		//retVal << "}" << endl; // already have the last closing bracket...

		return retVal.str();
	} else {
		return emptyFunctionCode();
	}
}

/**
* builds the code for the "int testCallValue(int x)" function
* @param s - the individual to construct from
* @return the code to put into the function
*/
string GAHandler::buildTestCallValueFunction(const string &s) {
	if (SettingsHandler::geSettings.phase == 1) {
		ostringstream retVal;
        
        // if this individual has an i in it, then we need to do a calculation with respect to i, so we need a loop
        if (s.find("i") != string::npos) {
    		vector<string> tokens = removeTokens(s);

            retVal << "\t" << "int i;" << endl;
            retVal << "\t" << "int * retVal = (int*)b_allocate(sizeof(int), x+1);" << endl;
            retVal << "\t" << "for(i = 0; i <= x; i++) {" << endl;
            retVal << "\t\t" << "if (" << tokens[0] << ") {" << endl; // the guard
            retVal << "\t\t\t" << "retVal[i] = " << tokens[1] << ";" << endl; // the op
            retVal << "\t\t" << "} else {" << endl;
            retVal << "\t\t\t" << "retVal[i] = INT_MIN;" << endl;
            retVal << "\t\t" << "}" << endl;
            retVal << "\t" << "}" << endl;
        // no i? well no need for a loop, duh
        } else {
            /* Create the calls to test the recursive case */
            retVal << "\t" << "int * retVal = (int*)b_allocate(sizeof(int), 1);" << endl;
            retVal << "\t" << "retVal[0] = " << s << ";" << endl;
        }
        retVal << "\t" << "return retVal;" << endl;

		return retVal.str();
	} else {
		return emptyFunctionCode();
	}
}

/**
* builds the code for the "int param(int x, int num)" function
* @param s - the individual to construct from
* @return the code to put into the function
*/
string GAHandler::buildParamFunction(const string &s) {
	if (SettingsHandler::geSettings.phase == 2){
		return SettingsHandler::paramFunction;
	} else {
		return emptyFunctionCode();
	}
}

/**
* builds the code for the "int recurseHelper(int x, int depth)" function
* @param s - the individual to construct from
* @return the code to put into the function
*/
string GAHandler::buildRecurseHelperFunction(const string &s) {
	return emptyFunctionCode();
}

#endif
