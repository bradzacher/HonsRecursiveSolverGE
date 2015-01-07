import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Coordinates and handles everything to do with running the GA
 * Can be run without the GUI for text-only command line runs
 */
public class RunHandler extends Thread {
	private int maxNumChildren;
	private String treeDataFile;
	private String grammarFile1;
	private String grammarFile2;
	private String outputFile;
	private String paramFile;
	
	private int distFormula = 1;

    /**
     * The total time taken
     */
	public float timeTaken;
    /**
     * The time taken to run phase 1
     */
    public float phase1TimeTaken;
    /**
     * The time taken to run phase 2
     */
    public float phase2TimeTaken;
    /**
     * The list of fitnesses from the run
     */
    public LinkedList<Float> fitnesses = new LinkedList<Float>();
    /**
     * The number of generations that were run in total
     */
    public int generations;
    /**
     * The number of generations phase 1 was run for
     */
    public int phase1Generations;
    /**
     * The number of generations phase 2 was run for
     */
    public int phase2Generations;
    
	/**
	* Basic GUI Constructor
	* @param maxNumChildren - the most number of children any one fragment can have
	* @param parent - the parent folder of the output files
	*/
	public RunHandler(int maxNumChildren, File parent) {
		super();
		
		File theFiles[] = TreeBuilder.createFileObjects(parent);
		
		this.maxNumChildren = maxNumChildren;
		int i = 0;
		this.treeDataFile = theFiles[i++].getAbsolutePath();
		this.grammarFile1 = theFiles[i++].getAbsolutePath();
		this.grammarFile2 = theFiles[i++].getAbsolutePath();
		this.outputFile   = theFiles[i++].getAbsolutePath();
		this.paramFile    = theFiles[i++].getAbsolutePath();
        
        this.timeTaken = 0;
		
		this.start();
	}
	
	/**
	* basic GUI-less Constructor
	* @param maxNumChildren - the most number of children any one fragment can have
	*/
	public RunHandler(int maxNumChildren, File parent, int distFormula, boolean threaded) {
		File theFiles[] = TreeBuilder.createFileObjects(parent);
		
		this.maxNumChildren = maxNumChildren;
		int i = 0;
		this.treeDataFile = theFiles[i++].getAbsolutePath();
		this.grammarFile1 = theFiles[i++].getAbsolutePath();
		this.grammarFile2 = theFiles[i++].getAbsolutePath();
		this.outputFile   = theFiles[i++].getAbsolutePath();
		this.paramFile    = theFiles[i++].getAbsolutePath();
        
        this.timeTaken = 0;
        
        this.distFormula = distFormula;
        
        Environment.GUI = false;
	}
	
	@Override
	public void run() {
		if (!TreeBuilder.wipeOutputFile(outputFile)) {
			return;
		}
		if (!TreeBuilder.wipeOutputFile(paramFile)) {
			return;
		}
		
		String phase1result[] = phase1();
		if (phase1result == null) {
			return;
		}
		
		if (!TreeBuilder.wipeOutputFile(outputFile)) {
			return;
		}
		if (!this.writeParamFile(paramFile, phase1result)) {
			return;
		}
		
		if (!Environment.GUI) {
			System.out.println("\n");
		}
		
		phase2(phase1result);
	}
	
	/**
	* figures out how many seconds were used to run from a set of output
	* @param lines the set of output
	*/
    private void addTimes(String lines[]) {
        Pattern p = Pattern.compile("Total time taken to run the GA (\\d+.\\d+) (milliseconds|seconds|minutes|hours).");
    	for(String s : lines) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                float time = Float.parseFloat(m.group(1));
                if (m.group(2).equals("milliseconds")) {
                    time /= 100;
                } else if (m.group(2).equals("minutes")) {
                    time *= 60;
                } else if (m.group(2).equals("hours")) {
                    time *= 3600;
                }
                this.timeTaken += time;
            }
        }
    }
	
	/**
	* pulls out the fitness scores of the individuals
	* @param lines the set of output
	*/
    private void grabFitness(String lines[]) {
        Pattern p = Pattern.compile("Best individual:\\(Fitness Score = (\\d+.\\d+|\\d+)\\)");
        for(String s : lines) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                this.fitnesses.addLast(Float.parseFloat(m.group(1)));
            }
        }
    }
	
	/**
	* figures out how many generations were used to run from a set of output
	* @param lines the set of output
	*/
    private void addGenerations(String lines[]) {
        Pattern p = Pattern.compile("Total number of generations: (\\d+) generations");
    	for(String s : lines) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                this.generations += Integer.parseInt(m.group(1));
            }
        }
    }

	
    /**
	* Runs the GA for phase 1
	* @return the resulting individual(s)
	*/
	private String[] phase1() {
        int popSize = 100;
        int numGens = 50;
		// we want to run it once for each child
		GEInterfaceFrame gei = new GEInterfaceFrame(1, treeDataFile, grammarFile1, outputFile, paramFile, numGens, popSize, distFormula);
		
		boolean containsI = false;
		String lineI[] = {""};
    	for (int i = 0; i < maxNumChildren; i++) {
    		gei.triggerRun();
    		if (gei.getExitValue() != 0) {
                gei.error();
    			return null;
    		}
            popSize += 50;
            gei.setPopulationSize(popSize);
            
            // if it contains an i term, then we only want this single result
            String lines[] = readFile(outputFile);
            if (lines[i].contains("i")) {
            	containsI = true;
            	lineI[0] = lines[i];
            	break;
            }
    	}
        
        // add the times on
        this.addTimes(gei.getText());
        this.phase1TimeTaken = this.timeTaken;
        
        // parse all the fitnessess
		this.grabFitness(gei.getText());
        
        // add the generations on
		this.addGenerations(gei.getText());
        this.phase1Generations = this.generations;
        
    	// read in the results
    	if (containsI) {
    		return lineI;
    	} else {
    		return readFile(outputFile);
    	}
	}
	
	
	/**
	* Runs the GA for phase 2
	* @param phase1result the results from phase 1
	*/
	private void phase2(String phase1result[]) {
		int phase = 2;
		// if we have an i, then we need a grammar that uses a loop
		if (phase1result[0].contains("i")) {
			File theFile = new File(grammarFile2);
			
			// construct the cpp code file
            String grammar_string = TreeBuilder.constructPhase2GrammarCode(phase1result[0]);
            
            // make sure it ends in .bnf
            if (!theFile.getName().endsWith(".bnf")) {
                theFile = new File(theFile.getParentFile().getAbsolutePath() + File.separator + theFile.getName() + ".bnf");
            }
            
            if (!TreeBuilder.saveToFile(theFile, grammar_string)) {
            	System.err.println("Could not update grammar with loop structure!");
            	System.exit(1);
            }
            
            phase = 3;
		}
		
        int numGens = 50  + (50 * phase1result.length);
        int popSize = 150 + (25 * phase1result.length);
		GEInterfaceFrame gei = new GEInterfaceFrame(phase, treeDataFile, grammarFile2, outputFile, paramFile, numGens, popSize, distFormula);
		gei.triggerRun();
		if (gei.getExitValue() != 0) {
			gei.error();
            return;
		}
		
		String lines[] = readFile(outputFile);
		String result = cleanUpSolution(lines, phase1result);
        
        // add the times on
        this.addTimes(gei.getText());
        this.phase2TimeTaken = this.timeTaken - this.phase1TimeTaken;
        
        // parse all the fitnessess
		this.grabFitness(gei.getText());
        
        // add the generations on
		this.addGenerations(gei.getText());
        this.phase2Generations = this.generations - this.phase1Generations;
        
		gei.printBreaker();
		gei.append("Phase 1 runtime: " + this.phase1TimeTaken + " seconds\n");
		gei.append("Phase 2 runtime: " + this.phase2TimeTaken + " seconds\n");
		gei.append("Phase 1 generations: " + this.phase1Generations + "\n");
		gei.append("Phase 2 generations: " + this.phase2Generations + "\n");
		gei.append("Solution Found (in " + String.format("%.3f", this.timeTaken) + " seconds):\n\n");
		gei.append(result);
	}
	
	/**
	 * Removes the GA helper methods and cleans up some of the useless math from a solution
	 * @param lines - the phase 2 result
	 * @param phase1result - the phase 1 result
	 * @return the clean solution
	 */
	private String cleanUpSolution(String lines[], String phase1result[]) {
		/*
		** remove doMaths from the phase1 results
		*/
		// using Pattern because it lets us easily get different parts of the string
		Pattern p = Pattern.compile("doMaths\\((.+), (\\d+), '(.+)'\\)");
		for (int i = 0; i < phase1result.length; i++) {
			Matcher m = p.matcher(phase1result[i]);
			if (m.matches()) {
				//String var = m.group(1);
				String val = m.group(2);
				String op  = m.group(3);
				// don't include the var because it mightn be correct for multi-var functions
				phase1result[i] = " " + op + " " + val;
			}
		}
		
		/*
		** replace all instances of "param" with their associated result calls
		*/
		// construct the patterns to look for
		String pattern = "(.+?)";
		for (int i = 0; i < phase1result.length; i++) {
			pattern += "(param\\(x, " + (i+1) + "\\))(.+?)";
		}
		Pattern p1 = Pattern.compile(pattern);
		Pattern p2 = Pattern.compile("param\\((.+?), \\d\\)");
		
		// look!
		for (int j = 0; j < lines.length; j++) {
			String s = lines[j];
			Matcher m1 = p1.matcher(s);
	        if (m1.matches()) { // see if this line is the one we want
	        	int paramNum = 0;
	        	int start = 0;
	        	String newS = "";
	        	
	        	// find the start and end of each param call
	        	for (int i = 1; i < m1.groupCount(); i++) {
	        		String s2  = m1.group(i);
   		        	Matcher m2 = p2.matcher(s2);

			    	if (m2.matches()) { //
			    		String var = m2.group(1); // the reason i use pattern in this instance - lets me pull out parts of the string
			    	
			    		newS += s.substring(start, m1.start(i));
			    		newS += var + phase1result[paramNum++];
			    		
			    		start = m1.end(i);
			    	}
			    }
			    
			    lines[j] = newS + s.substring(start);
	        }
		}
		
		
		/*
		** remove doMaths from the rest of the result
		*/
		p1 = Pattern.compile("(.*)(doMaths\\((.+), (.+), '(.+)'\\))(.*)");
		p2 = Pattern.compile("doMaths\\((.+), (.+), '(.+)'\\)");
		
		for (int i = 0; i < lines.length; i++) {
			Matcher m1 = null;
			
			// for as many times as this line has a doMaths in it..
			while ( (m1 = p1.matcher(lines[i])).matches() ) {
				for (int j = 1; j < m1.groupCount(); j++) {
					Matcher m2 = p2.matcher(m1.group(j));
					
					if (m2.matches()) {
						String var = m2.group(1);
						String val = m2.group(2);
						String op  = m2.group(3);
						
						int start = m1.start(j);
						int end = m1.end(j);
						String newLine = lines[i].substring(0, start);
						
						// remove useless maths from the eqn if there is some
						if ( (var.equals("1") || val.equals("1")) && (op.equals("*") || op.equals("/")) ) {
							if (var.equals("1")) {
								newLine += val;
							} else {
								newLine += var;
							}
						} else if ( (var.equals("0") || val.equals("0")) && (op.equals("+") || op.equals("-")) ) {
							if (var.equals("0")) {
								newLine += val;
							} else {
								newLine += var;
							}
						} else {
							newLine += "(" + var + " " + op + " " + val + ")";
						}
						
						newLine += lines[i].substring(end);
						lines[i] = newLine;
					}
				}
			}
		}
		
		/*
		** glue it all together with \n's
		*/
		String result = "";
		for (String s : lines) {
			result += s + "\n";
		}
		
		return result;
	}
	
	/**
	 * Reads in the given file and splits it by its lines
	 * @param filename - the file to read from
	 * @return the lines of the file
	 */
	private String[] readFile(String filename) {
    	LinkedList<String> lines = new LinkedList<String>();
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(filename));
    		String l = "";
    		while ( (l = br.readLine()) != null) {
    			if (l.length() > 0) {
    				lines.addLast(l);
    			}
    		}
    	} catch (IOException e) {
    		return null;
    	}
    	
    	return lines.toArray(new String[0]);
	}
	
	
	/**
	* Constructs the code for this run's "param(int x, int num)" function
	* @param paramFileName - the name of the file to write to
	* @param phase1result - the list of results from the phase 1 run
	* @return true if successfully written, false otherwise
	*/
	private boolean writeParamFile(String paramFileName, String phase1result[]) {
		File theFile = new File(paramFileName);
		
		String s = "";
		
		for (int i = 0; i < phase1result.length; i++) {
			if (i != 0) {
				s += " else ";
			}
			s += "if (num == " + (i+1) + ") {\n";
			s += "\treturn " + phase1result[i] + ";\n";
			s += "}";
		}
		
		return TreeBuilder.saveToFile(theFile, s);
	}

	/**
	 * Main method
	 * @param args - run with the --help arg to see full list
	 */
	public static void main(String args[]) {
		String help = "usage: java RunHandler <location to read everything from> <tree data filename> <optional: -n [number of times to run] -dist [0 for all-or-nothing, 1 for near-miss, 2 for square near-miss]>";
		for(String s : args) {
			if (s.equalsIgnoreCase("--help") || s.equalsIgnoreCase("-help") || s.equalsIgnoreCase("-h") || s.equalsIgnoreCase("--h")|| s.equalsIgnoreCase("/?")) {
				System.err.println(help);
				System.exit(0);
			}
		}
		if (args.length < 2 || args.length > 6) {
			System.err.println(help);
			System.exit(0);
		}
		int numRuns = 1;
		int distFormula = 1;
		if (args.length > 2) {
			for (int i = 2; i < args.length; i++) {
				if (args[i].equals("-n")) {
					try {
						numRuns = Integer.parseInt(args[++i]);
					} catch (NumberFormatException e) {
						System.err.println("Number of runs must be a valid integer!");
						System.exit(1);
					}
				} else if (args[i].equals("-dist")) {
					try {
						distFormula = Integer.parseInt(args[++i]);
						if (distFormula < 0 || distFormula  > 2) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException e) {
						System.err.println("Distance formula choice must be 0, 1 or 2!");
						System.exit(1);
					}
				}
			}
		}
		
		try {
			File parent = new File(args[0]);
			if (!parent.isDirectory()) {
				parent = parent.getParentFile();
			}
			File files[] = TreeBuilder.createFileObjects(parent);
			File treeFile = new File(parent, args[1]);
			
			//load the tree
			System.out.println("Loading Tree Data file: " + treeFile.getAbsolutePath());
    	    // Deserialize from a file
    	    ObjectInputStream in = new ObjectInputStream(new FileInputStream(treeFile));
    	    // Deserialize the object
    	    BuilderWindowPanel treeData = (BuilderWindowPanel) in.readObject();
    	    in.close();
    	    
    	    //
    	    System.out.println("Making sense of the Tree Data...");
    	    treeData.reposition();
    	    TreeBuilder tb = new TreeBuilder();
			
    	    //
    	    System.out.println("Writing CPP file: " + files[0].getAbsolutePath());
    	    tb.cppAction(files[0]);
    	    
    	    //
    	    System.out.println("Writing Phase 1 Grammar: " + files[1].getAbsolutePath());
    	    tb.grammarPhase1Action(files[1]);

    	    //
    	    System.out.println("Writing Phase 2 Grammar: " + files[2].getAbsolutePath());
    	    tb.grammarPhase2Action(files[2]);
    	    
    	    //
    	    String distFormulaName = "##error##";
    	    if (distFormula == 0) {
    	    	distFormulaName = "all-or-nothing";
    	    } else if (distFormula == 1) {
    	    	distFormulaName = "standard";
    	    } else if (distFormula == 2) {
    	    	distFormulaName = "squared";
    	    }
    	    System.out.println("Running " + numRuns + " times with \"" + distFormulaName + "\" distances.");
    	    
    	    // run all the times
    	    float phase1time = 0;
    	    float phase2time = 0;
    	    float phase1Correct = 0;
    	    float phase2Correct = 0;
    	    float phase1Fitness = 0;
    	    float phase2Fitness = 0;
    	    float phase1Count = 0;
    	    float phase2Count = 0;
    	    float phase1Generations = 0;
    	    float phase2Generations = 0;
    	    for (int i = 0; i < numRuns; i++) {
    	    	System.out.println("\n*********Run #" + (i+1) + "*********\n");
    	    	RunHandler rh = new RunHandler(Fragment.maxNumChildren(Environment.fragmentList), parent, distFormula, false);
    	    	rh.run();
    	    	phase1time += rh.phase1TimeTaken;
    	    	phase2time += rh.phase2TimeTaken;
    	    	
    	    	phase1Generations += rh.phase1Generations;
    	    	phase2Generations += rh.phase2Generations;
    	    	
    	    	for (int j = 0; j < (rh.fitnesses.size()-1); j++) {
	    			phase1Count += 1;
	    			phase1Correct += (rh.fitnesses.get(j) == 1) ? 1 : 0;
	    			phase1Fitness += rh.fitnesses.get(j);
    	    	}
    			phase2Count += 1;
    			phase2Correct += (rh.fitnesses.get(rh.fitnesses.size()-1) == 1) ? 1 : 0;
    			phase2Fitness += rh.fitnesses.get(rh.fitnesses.size()-1);
    	    }
    	    
    	    String s = "\n\n";
    		s += "----------------------------------------------------------------------------------------------------";
    		s += "\n\n";
    		s += "Times (sec)\n";
    	    s += String.format("%7s %8s %8s\n", "", "Total", "Average");
    	    s += String.format("%7s %8.4f %8.4f\n", "Phase 1", phase1time, (phase1time / numRuns));
    	    s += String.format("%7s %8.4f %8.4f\n", "Phase 2", phase2time, (phase2time / numRuns));
    		s += "\n\n";
    		s += "Fitnesses\n";
    	    s += String.format("%7s %10s %8s\n", "", "% correct", "Average");
    	    s += String.format("%7s %10.2f %8.4f\n", "Phase 1", (phase1Correct / phase1Count) * 100, (phase1Fitness / phase1Count));
    	    s += String.format("%7s %10.2f %8.4f\n", "Phase 2", (phase2Correct / phase2Count) * 100, (phase2Fitness / phase2Count));
    		s += "\n\n";
    		s += "Generations\n";
    	    s += String.format("%7s %8s %8s\n", "", "Total", "Average");
    	    s += String.format("%7s %8.0f %8.4f\n", "Phase 1", phase1Generations, (phase1Generations / numRuns));
    	    s += String.format("%7s %8.0f %8.4f\n", "Phase 2", phase2Generations, (phase2Generations / numRuns));
    	    
    	    System.out.println(s);
    	    
    	    // make a bell noise so we know it's done!
    	    GEInterfaceFrame.chime();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
