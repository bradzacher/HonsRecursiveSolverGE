/****************************************
* Conneciton Handler Function Defs      *
* Created by: Brad Zacher               *
* Computer Science Honours Project 2012 *
* Modified: 20/09/2012                  *
****************************************/
#ifndef _CONNECTIONHANDLER_CPP
#define _CONNECTIONHANDLER_CPP

#include "ConnectionHandler.h"

/**
* Variable Declarations
*/
int ConnectionHandler::socketfd = -1;
ostringstream ConnectionHandler::cmd;
ostringstream ConnectionHandler::gen;
ostringstream ConnectionHandler::data;
bool ConnectionHandler::connected = false;


/**
* Sends all the stats to the socket
*/
void ConnectionHandler::streamStats() {
    sendAverageScore(GAHandler::generationCounter, GAHandler::ga->statistics().online());
    sendBestScore(GAHandler::generationCounter, GAHandler::ga->statistics().bestIndividual().score());
    sendPhenotypeToSocket(GAHandler::generationCounter, GAHandler::ga->statistics().bestIndividual(), GAHandler::mapper);
}

/**
* Checks if there is a Connection
* @return true if connected, false otherwise
*/
bool ConnectionHandler::isConnected() {
    return ConnectionHandler::connected;
}

/**
* Connects to the grapher
* @return true if successful, false otherwise
*/
bool ConnectionHandler::connectToSocket() {
    int status;
    struct addrinfo hints;
    struct addrinfo *serverInfo;  /* will point to the results */

    memset(&hints, 0, sizeof hints); /* make sure the struct is empty */
    hints.ai_family = AF_UNSPEC;     /* don't care IPv4 or IPv6 */
    hints.ai_socktype = SOCK_STREAM; /* TCP stream sockets */

    /* get ready to connect */
    status = getaddrinfo("localhost", "2723", &hints, &serverInfo);
    if (status != 0) {
        return false;
    }
    
    /* make a socket: */
    ConnectionHandler::socketfd = socket(serverInfo->ai_family, serverInfo->ai_socktype, serverInfo->ai_protocol);
    if (socketfd == -1) {
        return false;
    }
    
    /* connect! */
    status = connect(ConnectionHandler::socketfd, serverInfo->ai_addr, serverInfo->ai_addrlen);
    if (status == -1) {
        return false;
    }
    ConnectionHandler::connected = true;
    
    return true;
}

/**
* Sends the average fitness to the socket (convenience method)
* @param generation - the generation number
* @param value - the average fitness
* @return true if successful, false otherwise
*/
bool ConnectionHandler::sendAverageScore(int generationCounter, double value) {
    return sendDoubleToSocket(AVERAGE_FITNESS_COMMAND, generationCounter, value);
}

/**
* Sends the best fitness to the socket (convenience method)
* @param generation - the generation number
* @param value - the best fitness
* @return true if successful, false otherwise
*/
bool ConnectionHandler::sendBestScore(int generationCounter, double value) {
    return sendDoubleToSocket(BEST_FITNESS_COMMAND, generationCounter, value);
}

/**
* Sends a double/float number to the socket (convenience method)
* @param command - the command
* @param generation - the generation number
* @param value - the double value
* @return true if successful, false otherwise
*/
bool ConnectionHandler::sendDoubleToSocket(int cmdI, int generationCounter, double value) {
    /* Convert the command to a string */
    cmd.str("");
    cmd << cmdI;
    
    /* Convert the generation to a string */
    gen.str("");
    gen << generationCounter;
    
    data.str("");
    data << value;
    
    return sendDataToSocket(cmd.str().c_str(), gen.str().c_str(), data.str().c_str());
}

/**
* Sends a given command, generation and data the socket
* @param command - the command
* @param generation - the generation number
* @param value - the data
* @return true if successful, false otherwise
*/
bool ConnectionHandler::sendDataToSocket(const char* command, const char* generation, const char* value) {
    if (socketfd != -1) {
        if (sendData(command) != -1) {
            if (sendData(generation) != -1) {
                if (sendData(value) != -1) {
                    return true;
                }
            }
        }
    }
    
    return false;
}

/**
* Sends a phenotype to the socket (convenience method)
* Note that this must be used to send a phenotype as sendDataToSocket cannot handle a value arg with "\n"
* @param generationCounter - the generation number
* @param ga - the GAGenome to map and send
* @return true if successful, false otherwise
*/
bool ConnectionHandler::sendPhenotypeToSocket(int generationCounter, const GAGenome & ga, GEGrammarSI mapper) {
    /* Convert the command to a string */
    cmd.str("");
    cmd << PHENOTYPE_COMMAND;
    
    /* Convert the generation to a string */
    gen.str("");
    gen << generationCounter;
    
    /* Convert the phenotype to a string */
    const GEListGenome &g = static_cast<const GEListGenome&>(ga);/* have to cast the GAGenome to a GEListGenome for mapping */
    mapper.setGenotype(g);
    data.str("");
    data << *(mapper.getPhenotype());

    return sendPhenotypeToSocket(cmd.str().c_str(), gen.str().c_str(), data.str());
}


/**
* Sends a phenotype to the socket
* @param command - the command (should be "2")
* @param generation - the generation number
* @param str - the mapped phenotype
* @return true if successful, false otherwise
*/
bool ConnectionHandler::sendPhenotypeToSocket(const char* command, const char* generation, const string str) {
    if (socketfd != -1) {
        if (sendData(command) != -1) {
            if (sendData(generation) != -1) {
            	string s(str);
                //cout << "\"" << s << "\"" << endl;

                /* Remove the delimiter from the phenotype */
                unsigned int loc;
                while ( (loc = s.find("/**/")) != string::npos ) {
                	s = (s.replace(loc, 4, ""));
                }

                //cout << "\"" << s << "\"" << endl;
                
                // if there is no \n at the end, then append one to make sure it gets sent properly
                if (s[s.size()-1] != '\n') {
                	s.push_back('\n');
                }

                unsigned int i = 0;
                int last = 0;
                for (i = 0; i < s.length(); i++) {
                    if (s[i] == '\n') {
                        string sub (s, last, (i+1 - last));
                        //cout << "\"" << sub << "\"" << endl;
                        if (sendData(sub.c_str()) == -1) {
                            return false;
                        }
                        last = i+1;
                    }
                }
                
                if (sendData("#END#") != -1) {
                    return true;
                }
            }
        }
    }
    
    return false;
}

/**
* Sends the string to the socket
* @param msg - the string to send
* @return true if successful, false otherwise
*/
bool ConnectionHandler::sendData(const char* msg) {
    int len = strlen(msg);
    
    ostringstream msgStr;
    msgStr << msg;
    
    if (msg[len-1] != '\n') {
        msgStr << '\n';
        len++;
    }
    
    send(socketfd, msgStr.str().c_str(), len, 0);
    
    char response[1024];
    recv(socketfd, response, 1024, 0);
    
    if (response[0] == '1') {
        return true;
    } else {
        return false;
    }
}

#endif
