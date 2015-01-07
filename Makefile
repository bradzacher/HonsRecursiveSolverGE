#########################################
# Makefile                              #
# Modified by: Brad Zacher              #
# Computer Science Honours Project 2012 #
# Modified: 19/06/12                    #
#########################################
# Uses the GALib evolutionary library
# Uses the libGE library

# The folder names
OBJ_DIR             = obj
SRC_DIR             = src
INC_DIR             = headers

# The objects
CPP_FILES           = main GAHandler GEListGenome SettingsHandler ConnectionHandler b_array_pp
C_FILES             = genomeHelper b_array

# Prepending the directory
CPP_OBJECTS         = $(patsubst %,$(OBJ_DIR)/%.o,$(CPP_FILES))
CPP_HEADERS         = $(patsubst %,$(INC_DIR)/%.h,$(CPP_FILES))
CPP_HEADER_OBJECTS  = $(patsubst %,$(INC_DIR)/%.h.gch,$(CPP_FILES))
C_OBJECTS           = $(patsubst %,$(OBJ_DIR)/%.o,$(C_FILES))
C_HEADERS           = $(patsubst %,$(INC_DIR)/%.h,$(C_FILES))
C_HEADER_OBJECTS    = $(patsubst %,$(INC_DIR)/%.h.gch,$(C_FILES))

# General compiler options
CC                = gcc
CXX               = g++
CFLAGS           += -Wall -O2
CXXFLAGS         += -Wall -O2

# Required libs
LIBS             += -lga -lGE -lrt -lm 

# ---

.PHONY: default all debug clean CPPPrint CPrint

default: all

all:  CHeaderPrint $(C_HEADER_OBJECTS) CPrint $(C_OBJECTS) CPPHeaderPrint $(CPP_HEADER_OBJECTS) CPPPrint $(CPP_OBJECTS) GEGCCPrint GEGCC

debug:  CXX += -DDEBUG
debug:  GEGCC $(EVAL_OBJECT)

# Compiles each of the objects
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp  $(INC_DIR)/%.h
	$(CXX) $(CXXFLAGS) -c -o $@ $< -iquote $(INC_DIR)

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.c 
	$(CC) $(CFLAGS) -c -o $@ $< -iquote $(INC_DIR)

$(CPP_HEADER_OBJECTS): $(CPP_HEADERS)
	$(CXX) $(CXXFLAGS) -c -o $@ $< -iquote $(INC_DIR)

$(C_HEADER_OBJECTS): $(C_HEADERS)
	$(CC) $(CFLAGS) -c -o $@ $< -iquote $(INC_DIR)

# Linker
GEGCC:  $(CPP_OBJECTS) $(CPP_HEADER_OBJECTS)
	$(CXX) -o $@ $(CPP_OBJECTS) $(LIBS)

# Some printlns for nicenes
CPPHeaderPrint:
	$(info )
	$(info --Precompiling C++ Headers--)
CHeaderPrint:
	$(info )
	$(info --Precompiling C Headers--)
CPPPrint:
	$(info )
	$(info --Assembling C++ Files--)
CPrint:
	$(info )
	$(info --Assembling C Files--)
GEGCCPrint:
	$(info )
	$(info --Linking C++ Files--)

clean:
	rm -f $(OBJ_DIR)/*.o is-output.dat $(INC_DIR)/*.h.gch
    
# ---