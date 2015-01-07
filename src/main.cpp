/****************************************
* main c++ program file                 *
* Modified by: Brad Zacher              *
* Computer Science Honours Project 2012 *
* Modified: 20/09/2012                  *
****************************************/
#ifndef _MAIN_CPP
#define _MAIN_CPP

#include "main.h"

int main(int argc, char **argv) {
	verboseOrNot(argc, argv);

    /* Print version of libGE used */
    SettingsHandler::println(string("Using libGE version ") + libGEVersion);
    SettingsHandler::println("");
    
    /* Try and connect to the grapher */
    SettingsHandler::println("Attempting to connect to the data display package.");
    if (ConnectionHandler::connectToSocket()) {
    	SettingsHandler::println("\tConnected to data display package.");
    } else {
    	SettingsHandler::println("\tUnable to connect to data display package.");
    }
    SettingsHandler::println("");
    
    SettingsHandler::println("Preparing the settings");
    /* Try and load the libGE settings file */
    SettingsHandler::println(string("\tLoading libGE settings file: '") + SettingsHandler::geSettings.settingsFileGE + "'");
    SettingsHandler::parseGESettings();
    
    /* Try and load the GALib settings file*/
    SettingsHandler::println(string("\tLoading GALib settings file: '") + SettingsHandler::geSettings.settingsFile + "'");
    SettingsHandler::parseGASettings();
    
    /* Parse command line parameters (if any) */
    if (argc > 1) {
    	SettingsHandler::println("\tParsing command line parameters.");
        
        SettingsHandler::parseGEcmd(argc, argv);
        
        SettingsHandler::parseGAcmd(argc, argv);
    }
    SettingsHandler::println("");
    
    /* Try and load a grammar file */
    SettingsHandler::println("Loading grammar file");
    SettingsHandler::println(string("\t") + SettingsHandler::loadGrammarFile(&GAHandler::mapper));
    SettingsHandler::println("");
    
    /* Load the wrappers */
    SettingsHandler::println("Loading wrapping files");
    SettingsHandler::loadWrappers();
    SettingsHandler::println("");
    
    /* Compile the TreeData File */
    SettingsHandler::println("Compiling Tree Data File");
    SettingsHandler::println(string("\t") + SettingsHandler::compileTreeDataFile());
    SettingsHandler::println("");

    /* Load the previously found individuals */
    SettingsHandler::println("Loading Found Individual List");
    SettingsHandler::println(string("\t") + SettingsHandler::readOutputList());
    SettingsHandler::println("");

    /* Apply all of the settings we've been given */
    SettingsHandler::println("Applying Settings");
    GAHandler::applySettings();
    SettingsHandler::println("");
    
    /* Running the GA */
    GAHandler::run();
    cout << endl;
    
    /* FINISHED */
    SettingsHandler::println("Finished running!");
    SettingsHandler::println("");
    
    /* Print stats */
    GAHandler::printStats(GAHandler::ga, GAHandler::mapper);
    SettingsHandler::println("");

    return 0;
}

void verboseOrNot(int argc, char ** argv) {
	int i = 0;
	for (i = 0; i < argc; i++) {
		if (strcmp(argv[i], "-nv") == 0) {
			SettingsHandler::verbose = false;
		}
	}
}

#endif
