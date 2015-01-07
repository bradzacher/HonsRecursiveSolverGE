import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
 
public class GAStatListener {
	private final boolean debug = false;
	private final boolean debugResponses = false;
	
	private enum Response {
		RECEIVED_COMMAND, RECEIVED_GENERATION, ADDED_VALUE, ACCEPTED_PHENOTYPE_PART, FAILED, EXIT;
	}
	private enum State {
		WAITING_FOR_COMMAND, WAITING_FOR_GENERATION, WAITING_FOR_DOUBLE, WAITING_FOR_INTEGER, WAITING_FOR_STRING;
	}
	public static enum Command {
		AVERAGE_FITNESS, BEST_FITNESS, BEST_PHENOTYPE, NOVALUE;
		
		public static Command toCommand(int i) {
            switch (i) {
            	case 0:
            		return AVERAGE_FITNESS;
            		
            	case 1:
            		return BEST_FITNESS;
            		
            	case 2:
            		return BEST_PHENOTYPE;
                
            	default:
            		return NOVALUE;
            }
        }
		
		public static Command toCommand(String s) {
			try {
                return valueOf(s);
            } catch (Exception e) {
                return NOVALUE;
            }
        }
	}
	
	
	private State currentState = State.WAITING_FOR_COMMAND;
	
	private final GAStatContainer gsc;
	
	private String lastAdded = "";
	
	public GAStatListener(GAStatContainer gsc) {
		this.gsc = gsc;
	}
	
    public void startListener() throws Exception {
	    ServerSocket serverSocket = null;
	    Socket clientSocket = null;
		BufferedReader in = null;
		PrintWriter out = null;

	    //Select the port to listening to
	    try {
	        serverSocket = new ServerSocket(2723);
	    } catch (IOException e) {
	        System.err.println("Could not listen on port: 2723.");
	        System.err.println(e);
	        System.exit(1);
	    }
		 
		while (true) {
			try {
			    //Start the listener
			    try {
			        clientSocket = serverSocket.accept();
			    } catch (IOException e) {
			        System.err.println("Accept failed.");
			        System.exit(1);
			    }
		 
			    //The reader that reads stuff from the port
			    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			    out = new PrintWriter(clientSocket.getOutputStream(), true);
			    System.out.println("CONNECTED");
			    if (debugResponses) {
			        out.println("CONNECTED");
			    } else {
			    	out.println("1");
			    }
			    
			    String inputLine = "";
		 
			    //Keep reading until we exit
			    while ((inputLine = in.readLine()) != null) {
			        Response r = this.processInput(inputLine);
					if (debug) {
						System.out.println(inputLine);
					}
			        String s = "";
					String debugStr = "";
			         
			        switch(r) {
			            case RECEIVED_COMMAND:
			                    debugStr = "RECEIVED_COMMAND: " + currentCommand;
								s = "1";
			                break;
			            
			            case RECEIVED_GENERATION:
			                    debugStr = "RECEIVED_GENERATION: " + generation;
								s = "1";
			                break;
			            
			            case ADDED_VALUE:
			                    debugStr = "ADDED_VALUE: " + lastAdded;
								s = "1";
			                break;
			                
			            case ACCEPTED_PHENOTYPE_PART:
			            		debugStr = "ACCEPTED_PHENOTYPE_PART: " + lastAdded;
			            		s = "1";
			        		break;
			                
			            case FAILED:
			                    debugStr = "INVALID INPUT: " + inputLine;
								s = "1";
			                break;
			                
			            case EXIT:
			                    debugStr = "EXIT";
								s = "1";
			                break;
			                
			            default:
			                    debugStr = "UNKNOWN_RESPONSE: " + r;
								s = "0";
			                break;
			        }
	
			        System.out.println(debugStr);
			        if (debugResponses) {           			
			             out.println(debugStr);
			        } else {
						 out.println(s);
					}
			        
			        if (r == Response.EXIT) {
			            out.close();
			            in.close();
			            clientSocket.close();
			            serverSocket.close();
			            System.exit(0);
			        }
			    }
			} catch (SocketException e) {
				System.out.println("CONNECTION TERMINATED BY CLIENT");
				System.out.println("AWAITING NEW CONNECTION...");
	            out.close();
	            in.close();
	            clientSocket.close();
			}
		}
    }
    
    /**
     * Processes a given input string and returns the response
     * @param s - the String to process
     * @return Response - the response from processing the string
     */
    Command currentCommand = Command.NOVALUE;
    int generation = -1;
	String bestPhenotype = "";
    private Response processInput(String s) {
    	int iVal;
    	double dVal;
    	String sVal;
    	
    	//
    	if (s.equalsIgnoreCase("exit")) {
    		return Response.EXIT;
    	}
    	
    	try {
	    	switch (currentState) {
	    		// Parse a command
	    		case WAITING_FOR_COMMAND:
	    				// Get the command number
	    				iVal = Integer.parseInt(s); 
	    				// Convert it to the enum
	    				currentCommand = Command.toCommand(iVal);
	    				// If it was an invalid command, FAIL OUT
	    				if (currentCommand == Command.NOVALUE) { 
	    					return reset();
	    				} else {
	    					// Switch the state
	    					this.currentState = State.WAITING_FOR_GENERATION; 
	    				}
    				return Response.RECEIVED_COMMAND;
	    			
				// Parse the generation number
	    		case WAITING_FOR_GENERATION:
	    				// Get the generation number
		    			generation = Integer.parseInt(s); 
		    			
		    			// Figure out what type we're waiting for
		    			switch (currentCommand) {
		    				// FITNESS = double
		    				case BEST_FITNESS:
		    				case AVERAGE_FITNESS:
		    						this.currentState = State.WAITING_FOR_DOUBLE;
		    					break;
		    				
                            // PHENOTYPE = String
		    				case BEST_PHENOTYPE:
		    						bestPhenotype = "";
		    						this.currentState = State.WAITING_FOR_STRING;
	    						break;
		    					
	    					// NOVALUE = RESET
		    				default:
		    					return reset();
		    			}
	    			return Response.RECEIVED_GENERATION;
	    			
    			// Parse a double value
	    		case WAITING_FOR_DOUBLE:
	    				dVal = Double.parseDouble(s);
	    				// Construct the queue string
	    				lastAdded = currentCommand + " " + generation + " " + dVal;
	    				// Add it
	    				switch (currentCommand) {
		    				case AVERAGE_FITNESS:
		    						gsc.addAverageFitnessPoint((double)generation, dVal);
		    					break;
		    					
		    				case BEST_FITNESS:
	    							gsc.addBestFitnessPoint((double)generation, dVal);
    							break;
		    				
	    					// NOVALUE = RESET
		    				default:
		    					return reset();
		    			}
	    				
	    				// Reset the state variables
	    				reset();
	    			return Response.ADDED_VALUE;
	    			
    			// Parse an integer value
	    		/*case WAITING_FOR_INTEGER:
		    			iVal = Integer.parseInt(s);
		    			// Construct the queue string
		    			lastAdded = currentCommand + " " + generation + " " + iVal;
		    			// Add it
	    				switch (currentCommand) {
		    				case RUN_TIME:
		    						gsc.addRunTimePoint(generation, iVal);
		    					break;
		    				
	    					// NOVALUE = RESET
		    				default:
		    					return reset();
		    			}
	    				// Reset the state variables
	    				reset();
	    			return Response.ADDED_VALUE;*/
                        
    			//Parse a String value
	    		case WAITING_FOR_STRING:
		    			sVal = s;
		    			// Construct the queue string
		    			lastAdded = currentCommand + " " + generation + " " + sVal;
		    			// Add it
	    				switch (currentCommand) {
		    				case BEST_PHENOTYPE:
		    						if(sVal.equalsIgnoreCase("#END#")) {
		    			    			// Construct the queue string
		    			    			lastAdded = currentCommand + " " + generation + " " + bestPhenotype;
		    							gsc.setBestPhenotype(bestPhenotype);
		    							break;
		    						}
		    						bestPhenotype += sVal + "\n";
		    					return Response.ADDED_VALUE;
		    				
		    				// NOVALUE = RESET
		    				default:
		    					return reset();
		    			}
	    				// Reset the state variables
	    				reset();
	    			return Response.ADDED_VALUE;
	    		
	    		// If we're in an invalid state, RESET
    			default:
					return reset();
	    	}
    	} catch (Exception e) {
			System.err.println("EXCEPTION: " + e);
    		// If a parse went wrong, just RESET
    		return reset();
    	}
    }
    
    /**
     * Resets the listener's various states
     * @return Response.FAILED
     */
    private Response reset() {
    	currentState = State.WAITING_FOR_COMMAND;
    	currentCommand = Command.NOVALUE;
    	generation = -1;
		return Response.FAILED;
	}
}
