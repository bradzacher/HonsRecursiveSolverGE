import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

//import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
//import javax.swing.border.BevelBorder;

/**
 * Handles all of the input and output processing relating to the actual Genetic Algorithm
 */
public class GEInterfaceFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	// awt stuff
	private final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private final int NUM_COLS = 105;
	private final int NUM_ROWS = 30;
	private final Dimension MAX_DIMENSION = new Dimension(720, 650);
	
	private GEInterface gei = null;
	
	private String args[];
	private TextAreaPrintStream text;
	private boolean finished = true;
	
	/**
	 * Basic constructor
	 * @param phase - the phase to run the GA in
	 * @param treeDataFile - the treeData.c file (full path)
	 * @param grammarFile - the grammar file to use (full path)
	 * @param outputFile - the output file to use (full path)
	 * @param paramFile - the param.c file to use (full path)
	 * @param generations - the number of generations to run for
	 * @param populationSize - the size of the population to run with
	 * @param distFormula - the distance formula to use
	 */
	public GEInterfaceFrame(int phase, String treeDataFile, String grammarFile, String outputFile, String paramFile, int generations, int populationSize, int distFormula) {
		super("Genetic Algorithm: Phase #" + ((phase==3)?2:phase) + " Output");
		
		if (Environment.GUI) {
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			// position in the middle of the screen
	        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
			this.setLocation(p.x - (MAX_DIMENSION.width / 2), p.y - (MAX_DIMENSION.width / 2));
	        
	        // setup the content pane
			JTextArea textArea = new JTextArea();
			this.text = new TextAreaPrintStream(textArea);
	        DefaultCaret caret = (DefaultCaret)textArea.getCaret();
	        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	        
	        textArea.setEditable(false);
	        textArea.setColumns(NUM_COLS);
	        textArea.setRows(NUM_ROWS);
	        textArea.setFont(TEXT_FONT);
	        textArea.setWrapStyleWord(false);
	        textArea.setLineWrap(true);
			
			JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			
			//scroll.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			
			this.setContentPane(scroll);
			
			this.setMinimumSize(MAX_DIMENSION);
			this.setMaximumSize(MAX_DIMENSION);
			this.setPreferredSize(MAX_DIMENSION);
			
		    //Show the window.
		    this.pack();
		    this.validate();
		    this.setVisible(true);
		} else {
		    this.text = new TextAreaPrintStream(null);
		}
	    
	    String args[] = {
	    		"phase " + phase,
	    		"treeDataFile " + treeDataFile,
	    		"grammarFile " + grammarFile,
	    		"outputFile " + outputFile,
	    		"paramFile " + paramFile,
	    		"number_of_generations " + generations,
	    		"population_size " + populationSize,
	    		"distanceFormula " + distFormula
	    };
	    this.args = args;
	    
	    if (phase == 1) {
	    	text.append("Running Phase 1 Grammatical Evolution Evolution");
	    } else if (phase == 2 || phase == 3) {
	    	text.append("Running Phase 2 Grammatical Evolution Evolution");
		}
	}
    
	/**
	 * Changes the number of generations
	 * @param numGens - the value to change to
	 */
	public void setNumberOfGenerations(int numGens) {
        args[5] = "number_of_generations " + numGens;
    }
	
	/**
	 * Changes the size of the population
	 * @param popSize - the value to change to
	 */
    public void setPopulationSize(int popSize) {
        args[6] = "number_of_generations " + popSize;
    }
    
	/**
	* Creates a new GEInterface instance which in turn starts a run using the given params
	* NOTE - SUBSEQUENT CALLS TO triggerRun() will do nothing until the previous run is finished
	*/
	public void triggerRun() {
		if (this.finished) {
			if (this.text.length() != 0) {
				printBreaker();
			}
			gei = GEInterface.GEInterfaceFactory(text, args);
			this.finished = false;
		}
	}
	
	/**
	* Prints a dividng line into the text box
	*/
	public void printBreaker() {
		String s = "\n\n";
		s += "----------------------------------------------------------------------------------------------------";
		s += "\n\n";
		this.append(s);
	}
	
	/**
	* Appends a string onto the textbox
	* @param s - the string to append
	*/
	public void append(String s) {
		this.text.append(s);
        //this.text.setCaretPosition(this.text.getDocument().getLength()-1);
        //int l = this.text.getDocument().getLength();
        //this.text.select(l, l); 
	}
    
    /* JTextAreas can't do html formatting and i'm too lazy to implement a styled box
    public void appendWithFormatting(String s, String formats) {
        String prefix = "<html>";
        String suffix = "</html>";
        for(char c : formats.toCharArray()) {
            switch (c) {
                case 'b':
                        prefix += "<b>";
                        suffix += "</b>";
                    break;
                
                case 'i':
                        prefix += "<i>";
                        suffix += "</i>";
                    break;
                
                case 'u':
                        prefix += "<u>";
                        suffix += "</u>";
                    break;
            }
        }
        this.append(prefix + s + suffix);
    }
    */
    
    /**
    * Appends an error message to the text area
    */
    public void error() {
        this.printBreaker();
        this.append("An error has occurred.");
    }
    
    /**
    * Returns all of the text currently in this frame's TextArea
    * @return an array of the string split around "\n"
    */
    public String[] getText() {
        return this.text.getText();
    }
	
	/**
	* Waits for the associated process to finish before returing its exit code
	* @return the exit code
	*/
	public int getExitValue() {
		int val = gei.getExitValue();
		this.finished = true;
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		
		return val;
	}
	
	/**
	 * Plays a chime sound
	 */
	public static void chime() {
		// one of these should work....
		// but for some reason they just refuse to
		//java.awt.Toolkit.getDefaultToolkit().beep();
		//System.out.println((char)7);
		//System.out.print("\0007");
	    //System.out.flush();
	}
	
	/*
	public static void main(String args[]) {
		GEInterfaceFrame temp = new GEInterfaceFrame(
				1,
				"/home/brad/honours/trees/fibonacci/treeData.c",
				"/home/brad/honours/trees/fibonacci/grammar1.bnf",
				"/home/brad/honours/trees/fibonacci/alreadyFound.txt",
				"/home/brad/honours/trees/fibonacci/param.c",
				50,
				150);
		temp.triggerRun();
		temp.getExitValue();
		
		temp.triggerRun();
		temp.getExitValue();
		
		GEInterfaceFrame.chime();
	}
	*/
}
