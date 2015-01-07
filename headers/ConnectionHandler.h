/*****************************************
* Connection Handler Definition          *
*                                        *
* Stores everything relating to the      *
*    connection to the grapher.          *
*                                        *
* Created by: Brad Zacher                *
* Computer Science Honours Project 2012  *
* Modified: 20/09/2012                   *
*****************************************/
#ifndef _CONNECTIONHANDLER_H
#define _CONNECTIONHANDLER_H

#include <iostream>
#include <sstream>
#include <string>

/* Network Libraries */
#include <sys/socket.h>
#include <netdb.h>

/* the GALib and libGE libraries */
#include <ga/ga.h>
#include <GE/ge.h>
/* Genome definition */
#include "GEListGenome.h"
#include "GAHandler.h"

/* The various commands to tell the grapher what to do */
#define AVERAGE_FITNESS_COMMAND 0
#define BEST_FITNESS_COMMAND 1
#define PHENOTYPE_COMMAND 2

using namespace std;

class ConnectionHandler {
    public:
        /** VARS **/
        static int socketfd;
        static ostringstream cmd;
        static ostringstream gen;
        static ostringstream data;
        static bool connected;
    
        /** METHODS **/
        static bool connectToSocket();
        static bool isConnected();
    
        static void streamStats();
    
        static bool sendAverageScore(int, double);
        static bool sendBestScore(int, double);
    
        static bool sendDoubleToSocket(int, int, double);
        static bool sendDataToSocket(const char *, const char *, const char *);
    
        static bool sendPhenotypeToSocket(int, const GAGenome &, GEGrammarSI);
        static bool sendPhenotypeToSocket(const char *, const char *, const string);
    
        static bool sendData(const char *);
};

#endif
