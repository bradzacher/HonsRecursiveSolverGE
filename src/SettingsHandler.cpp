/****************************************
* Settings Handler Function Defs        *
* Created by: Brad Zacher               *
* Computer Science Honours Project 2012 *
* Modified: 20/09/2012                  *
****************************************/
#ifndef _SETTINGSHANDLER_CPP
#define _SETTINGSHANDLER_CPP

#include "SettingsHandler.h"

/* The default libGE settings */
const struct geSettingsStruct DEFAULT_GESETTINGS = {
		0,
		0,
		false,
		15,
		25,
		0.5,
		10,
		0,
		0.0,
		true,
		"wrappers/",
		"grammars/recursiveGrammar.bnf",
		"outputFiles/treeData.c",
		"outputFiles/alreadyFound.txt",
		"outputFiles/param.c",
		1,
		1,
        20,
		"settingsGA.txt",
		"settingsGE.txt"
};
#define REC_GRAMMAR_LOC "grammars/recursiveGrammar.bnf"
#define BASE_GRAMMAR_LOC "grammars/baseGrammar.bnf"

/**
* Variable Declarations
*/
char SettingsHandler::Wrapper1[WRAPPER_LENGTH];
char SettingsHandler::Wrapper2[WRAPPER_LENGTH];
char SettingsHandler::Wrapper3[WRAPPER_LENGTH];
char SettingsHandler::Wrapper4[WRAPPER_LENGTH];
char SettingsHandler::Wrapper5[WRAPPER_LENGTH];
char SettingsHandler::paramFunction[WRAPPER_LENGTH];
geSettingsStruct SettingsHandler::geSettings = DEFAULT_GESETTINGS;
GAParameterList SettingsHandler::gaParams;
bool SettingsHandler::verbose = true;
string * SettingsHandler::alreadyFound = NULL;
int SettingsHandler::numAlreadyFound = 0;
string SettingsHandler::treeDataObject = string();

/**
* Loads in a grammar file and also saves a copy of the grammar
* @param grammarFile - the grammar file to load
* @param mapper - the mapper to load into
*/
string SettingsHandler::loadGrammarFile(GEGrammarSI * mapper) {
    mapper->setMaxWraps(geSettings.wrappingEvents);
    
    if (!mapper->readBNFFile(geSettings.grammarFile)) {
    	stringstream s;
    	s << "Could not read " + geSettings.grammarFile;
    	SettingsHandler::println(true, s.str());
    	SettingsHandler::println(true, "Execution aborted.");
        exit(0);
    }

    return geSettings.grammarFile;
}

/**
* Loads in all of the wrapper files
*/
string loadWrapperFile (string filename, char * code) {
    FILE *c_code;
    char buffer[WRAPPER_LENGTH];

    strcpy(code,"");

	if(!(c_code=fopen(filename.c_str(), "r"))) { /* Try to open the start file */
		SettingsHandler::println(cerr, string("Could not read ") + filename);
		SettingsHandler::println(cerr, string("Execution aborted."));
		exit(0);
	}

	while(fgets(buffer,WRAPPER_LENGTH,c_code)) {/* Read in the entire file */
		strcat(code,buffer);
	}

	fclose(c_code);

	return filename;
}
void SettingsHandler::loadWrappers() {
    /* Read in the recursive wrapper code files */
	SettingsHandler::println(string("\t") + loadWrapperFile(geSettings.wrapperFolder + "1.c", Wrapper1));
	SettingsHandler::println(string("\t") + loadWrapperFile(geSettings.wrapperFolder + "2.c", Wrapper2));
	SettingsHandler::println(string("\t") + loadWrapperFile(geSettings.wrapperFolder + "3.c", Wrapper3));
	SettingsHandler::println(string("\t") + loadWrapperFile(geSettings.wrapperFolder + "4.c", Wrapper4));
	SettingsHandler::println(string("\t") + loadWrapperFile(geSettings.wrapperFolder + "5.c", Wrapper5));

	SettingsHandler::println(string("\t") + loadWrapperFile(geSettings.paramFile, paramFunction));
}

/**
* Used to compile the treedata file
*/
string SettingsHandler::compileTreeDataFile() {
	string filename_c   = SettingsHandler::geSettings.treeDataFile;
	string filename_o   = filename_c.substr(0,filename_c.find_last_of('.')) + ".o";
	ostringstream cmd;
	cmd << "gcc -w -c -o " << filename_o << " " << filename_c << " -iquote headers";
	if (system(cmd.str().c_str()) != 0) {
		SettingsHandler::println(cerr, string("treeData compilation failed.\n"));
		exit(0);
	}

	SettingsHandler::treeDataObject = filename_o;

	return filename_c + " to " + filename_o;
}

/**
* Used to load params from the GE settings file
* THIS WILL WIPE ALL SETTINGS IN THE geSettings STRUCT
* @return true on success, false otherwise
*/
void SettingsHandler::parseGESettings() {
    geSettings = DEFAULT_GESETTINGS;
    if (geSettings.settingsFileGE.size() > 0) {
        #define LINE_LENGTH 500
        #define TOTAL_NUM_SETTINGS 36
        
        char buffer[LINE_LENGTH];
        char * token;
        char * ptr;
        char ** parsedParams = (char **)malloc(sizeof(char*) * TOTAL_NUM_SETTINGS);

        FILE * geSettingsFile;

        int i = 0;
        int j = 0;
        int k = 0;
        int tokenLength = 0;
        int numTokens = 0;

        bool skipLine = false;

        /* Read GE Settings file and save settings appropriately */
        if(!(geSettingsFile = fopen(geSettings.settingsFileGE.c_str(), "r"))) {
            free(parsedParams);
            return;
        }
        while (fgets(buffer, LINE_LENGTH, geSettingsFile)) {
            for (i = 0; i < LINE_LENGTH; i++) {
                ptr = &buffer[i];

                j = i;
                /* as soon as we find a #, \0, or newline, we know the rest of the buffer is garbage */
                while ( !(skipLine = (buffer[j] == '#') || (buffer[j] == '\0') || (buffer[j] == '\n') || (buffer[j] == '\r')) ) {
                    if ( (buffer[j] == ' ') || (buffer[j] == '\t') ) {
                        break;
                    }
                    j++;
                }

                /* if j == i, then the only char we've seen is a space or a tab */
                if (j > i) {
                    tokenLength = j - i + 1;
                    token = (char *)malloc(sizeof(char) * tokenLength); /* allocate the memory for the token */
                    for (k = 0; k < tokenLength; k++) {
                        token[k] = '\0';
                    }
                    strncpy(token, ptr, tokenLength-1); /* copy the token over */

                    parsedParams[numTokens] = token; /* store the token in the return array */

                    numTokens++;
                }

                /* if the boolean is true, then we don't want to process any more of this line */
                if (skipLine == true) {
                    break;
                }

                i = j;
            }
        }
        fclose(geSettingsFile); /* no longer need to have the file open */

        /* Parse settings file containing options not taken care by GAParameterList */
        int exit_code = 0;
        if ((exit_code = parseGEcmd(TOTAL_NUM_SETTINGS, parsedParams)) != -1) {
        	stringstream s;
        	s << "An error occurred on reading line " << exit_code << " of " << geSettings.settingsFileGE;
        	SettingsHandler::println(cerr, s.str());
			exit(1);
		}

        for(i = 0; i < (TOTAL_NUM_SETTINGS - 1); i++) {
            free(parsedParams[i]); /* we're done with this element, so we can free it */
        }
    }
    
	#undef LINE_LENGTH
	#undef TOTAL_NUM_SETTINGS
}

/**
* Used to parse the libGE specific parameters from the cmd line
* @param argc - the number of command line arguments
* @param argv - the array of arguments
*/
int SettingsHandler::parseGEcmd(int argc, char ** parsedParams) {
	#define STRTOBOOL(A) ((strcmp(A, "true") == 0) ? true : false)
    int i = 0;
    bool everything_okay = true;

    bool validNumber = false;
    bool validBoolean = false;
    bool foundParam = false;
    
    for(i = 0; i < argc-1; i++) {
        validNumber = ((strcspn(parsedParams[i+1], "0123456789") == 0) ? true : false);
        validBoolean = (strcmp(parsedParams[i+1], "true") == 0 || strcmp(parsedParams[i+1], "false") == 0);

        if (parsedParams[i] == NULL) {
			continue;
		} else if (strcmp(parsedParams[i], "seed") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.seed            = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "wrappingEvents") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.wrappingEvents  = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "sensibleInit") == 0) {
			i++;
			foundParam = true;
			if (!validBoolean) {
				everything_okay = false;
			} else {
				geSettings.sensibleInit    = STRTOBOOL(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "minSize") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.minSize         = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "maxSize") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.maxSize         = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "grow") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.grow            = atof(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "maxDepth") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.maxDepth        = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "tailSize") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.tailSize        = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "tailRatio") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.tailRatio       = atof(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "effectiveXO") == 0) {
			i++;
			foundParam = true;
			if (!validBoolean) {
				everything_okay = false;
			} else {
				geSettings.effectiveXO     = STRTOBOOL(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "wrapperFolder") == 0) {
			i++;
			foundParam = true;
			ostringstream s;
			s << parsedParams[i];
			geSettings.wrapperFolder       = s.str();
		} else if (strcmp(parsedParams[i], "grammarFile") == 0) {
			i++;
			foundParam = true;
			ostringstream s;
			s << parsedParams[i];
			geSettings.grammarFile         = s.str();
		} else if (strcmp(parsedParams[i], "treeDataFile") == 0) {
			i++;
			foundParam = true;
			ostringstream s;
			s << parsedParams[i];
			geSettings.treeDataFile        = s.str();
		} else if (strcmp(parsedParams[i], "outputFile") == 0) {
			i++;
			foundParam = true;
			ostringstream s;
			s << parsedParams[i];
			geSettings.outputFile          = s.str();
		} else if (strcmp(parsedParams[i], "paramFile") == 0) {
			i++;
			foundParam = true;
			ostringstream s;
			s << parsedParams[i];
			geSettings.paramFile           = s.str();
		} else if (strcmp(parsedParams[i], "phase") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.phase           = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "distanceFormula") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.distanceFormula = atoi(parsedParams[i]);
			}
		} else if (strcmp(parsedParams[i], "maxTimeMS") == 0) {
			i++;
			foundParam = true;
			if (!validNumber) {
				everything_okay = false;
			} else {
				geSettings.maxTimeMS = atoi(parsedParams[i]);
			}
		}

        if (!everything_okay) {
            return i;
        }
	}

    return -1;

	#undef STRTOBOOL
}

/**
* Used to load params from the settings file
* THIS WILL WIPE ALL SETTINGS IN THE gaParams OBJECT
* @return true on success, false otherwise
*/
void SettingsHandler::parseGASettings() {
    GASteadyStateGA::registerDefaultParameters(gaParams);
    if (geSettings.settingsFile.size() > 0) {
        if (gaParams.read(geSettings.settingsFile.c_str()) != 0) {
            return;
        } else {
        	SettingsHandler::println(cerr, string("\tUnable to load the GALib settings file."));
            exit(1);
        }
    }
}

/**
* Used to parse the GALib specific parameters from the cmd line
* @param argc - the number of command line arguments
* @param argv - the array of arguments
*/
void SettingsHandler::parseGAcmd(int argc, char ** argv) {
    gaParams.parse(argc, argv, gaFalse);
}

/**
* Used to print a line to cout iff verbose mode is on (convenience method)
* @param str - the string to print
*/
void SettingsHandler::println(string str) {
	SettingsHandler::println(false, str);
}

/**
* Used to print a line to the ostream out iff verbose mode is on
* @param out - the ostream to print to
* @param str - the string to print
*/
void SettingsHandler::println(bool err, string str) {
	if (SettingsHandler::verbose) {
		if (err) {
			cerr << str << endl;
		} else {
			cout << str << endl;
		}
	}
}

void SettingsHandler::outputIndividual(const GAGenome &g, GEGrammarSI mapper) {
    GAListGenome<unsigned char> &genome = (GAListGenome<unsigned char> &) g;
    /* Assign genotype to mapper */
    mapper.setGenotype(genome);

    ofstream outfile;
    outfile.open(SettingsHandler::geSettings.outputFile.c_str(), ios::app);
    outfile << *(mapper.getPhenotype()) << endl;
    outfile.close();
}

string SettingsHandler::readOutputList() {
    vector<string> lines;
    string line;

    ifstream infile;
    infile.open(SettingsHandler::geSettings.outputFile.c_str(), ios::in);
    while (infile.good()) {
    	getline(infile, line);
    	if (line.size() > 0) {
    		lines.push_back(string(line));
    	}
    }
    infile.close();

    SettingsHandler::numAlreadyFound = lines.size();

    if (lines.size() == 0) {
    	return SettingsHandler::geSettings.outputFile;
    }

    SettingsHandler::alreadyFound = new string[lines.size()];
    unsigned int i = 0;
    for (i = 0; i < lines.size(); i++) {
    	SettingsHandler::alreadyFound[i] = string(lines[i]);
    }

    return SettingsHandler::geSettings.outputFile;
}

bool SettingsHandler::checkPhenotype(const Phenotype p) {
	if (SettingsHandler::numAlreadyFound == 0) {
		return false;
	}

	ostringstream s;
	s << p;
	int i = 0;
	for (i = 0; i < SettingsHandler::numAlreadyFound; i++) {
		if (SettingsHandler::alreadyFound[i].compare(s.str()) == 0) {
			return true;
		}
	}
	return false;
}

#endif
