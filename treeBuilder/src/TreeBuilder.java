import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Handles everything relating to actually building the trees to usable files
 */
public class TreeBuilder {
	private JDialog dialog;
    private JButton okBtn;
    public static JFileChooser chooser;
    private JCheckBox checkBoxes[] = new JCheckBox[3];
    
	public static final Dimension horizontalGlueSize = new Dimension(10,0);
	public static final Dimension horizontalGlueSize2 = new Dimension(5,0);
	
	private final Dimension MAX_DIMENSION = new Dimension(800, 800);
	
	public static final int NUM_ITEMS = 3;

	private JTextField locationText;
	
	/**
	* Standard Constructor
	* @param parentFrame the parent frame..
	*/
	public TreeBuilder() {
		Environment.constructFragmentList();
		
		if (Environment.GUI) {
			this.setupDialogBox();
		}
	}
	
	/**
	* Creates the list of files used by the builder/GE
	* @param parent the root folder for the files
	* @return the list of files
	*/
	public static File[] createFileObjects(File parent) {
		File theFiles[] = new File[NUM_ITEMS+2];
		theFiles[0] = new File(parent, "treeData.c");
		theFiles[1] = new File(parent, "grammar1.bnf");
		theFiles[2] = new File(parent, "grammar2.bnf");
		theFiles[3] = new File(parent, "alreadyFound.txt");
		theFiles[4] = new File(parent, "param.c");
		return theFiles;
	}

	/**
	* Shows the constructed JDialog
	*/
	public void popupDialog() {
		// put SOMETHING in the box..
		String str = Environment.lastOpenLocation.getAbsolutePath();
		if (Environment.lastBuildFolder != null) {
			str = Environment.lastBuildFolder.getAbsolutePath();
		}
		locationText.setText(str);
		// pack the window and make sure it's in the middle of the screen
		dialog.pack();
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        dialog.setLocation(p.x - (dialog.getMinimumSize().width / 2), p.y - (dialog.getMinimumSize().height / 2));
    	dialog.setVisible(true);
	}
	
	/**
    * Constructs the build action popup window
    */
    private void setupDialogBox() {
        // create a new dialog window
    	dialog = new JDialog(null, "Build Tree", ModalityType.APPLICATION_MODAL);
    	
    	JPanel checkBoxRow = buildCheckBoxRow();
    	JPanel fileRow = buildFileRow();
    	JPanel buttonRow = buildButtonRow();
  	
    	JPanel dialogContent = new JPanel();
    	dialogContent.setLayout(new BoxLayout(dialogContent, BoxLayout.Y_AXIS));
    	
    	dialogContent.add(Box.createVerticalGlue());
    	dialogContent.add(checkBoxRow);
    	dialogContent.add(Box.createVerticalGlue());
    	dialogContent.add(fileRow);
    	dialogContent.add(Box.createVerticalGlue());
    	dialogContent.add(buttonRow);
    	dialogContent.add(Box.createVerticalGlue());
        
    	// adda ll the content
        dialog.setContentPane(dialogContent);
    	
        dialog.getRootPane().setDefaultButton(okBtn);
        
    	// position in the middle of screen
    	dialog.setMinimumSize(new Dimension(MAX_DIMENSION.width, 150));
    	dialog.setLocationRelativeTo(null);
    	
    	// we don't want to ever actually close this window, just hide it
    	dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    	dialog.addWindowListener(new WindowAdapter() {
    	    public void windowClosing(WindowEvent we) {
    	    	we.getWindow().setVisible(false);
    	    }
    	});
    }
	
    /**
    * Constructs a row to insert into the build action popup window
    */
	private JPanel buildFileRow() {
    	// create the elements
    	JLabel label = new JLabel("Select a folder to build to");
    	locationText = new JTextField("", 40);
    	locationText.setMaximumSize(new Dimension(50, 25));
    	
    	JButton browse = new JButton("Browse...");
    	
    	// setup the onclick action
    	browse.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			// if we haven't opened a file, just open the default last open location
    			File temp = Environment.lastOpenLocation;
    			
    			// if a file was already selected, then use that as the "last open location"
    			if (locationText != null) {
    				if (locationText.getText().length() > 0) {
    					temp = new File(locationText.getText());
    				}
    			}
    			
    			// popup the dialog
    			temp = popupSaveDialog(temp, null, null, "Select a folder to build to...", "Select Location", true);
    			
    			// if a file was selected, save it
    			if (temp != null) {
    				locationText.setText(temp.getAbsolutePath());
    			}
    		}
    	});
    	
    	JPanel row = new JPanel();   	
    	
    	// horizontal box layout
    	row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
    	
    	// add the elements
    	row.add(Box.createHorizontalGlue());
    	row.add(label);
    	row.add(Box.createRigidArea(horizontalGlueSize2));
    	row.add(locationText);
    	row.add(Box.createRigidArea(horizontalGlueSize));
    	row.add(browse);
    	row.add(Box.createHorizontalGlue());
    	
    	return row;
    }
	
	/**
    * Constructs a row to insert into the build action popup window
    */
	private JPanel buildCheckBoxRow() {
		JLabel title = new JLabel("Build the following items:");
		checkBoxes[0] = new JCheckBox("CPP File", true);
		checkBoxes[1] = new JCheckBox("Phase 1 Grammar File", true);
		checkBoxes[2] = new JCheckBox("Phase 2 Grammar File", true);
		
		JPanel col = new JPanel();
    	// vertical box layout
		col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
		
		col.add(title);
		col.add(checkBoxes[0]);
		col.add(checkBoxes[1]);
		col.add(checkBoxes[2]);
		
    	JPanel row = new JPanel();
    	// horizontal box layout
    	row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

    	row.add(Box.createHorizontalGlue());
    	row.add(col);
    	row.add(Box.createHorizontalGlue());
    	
    	return row;
	}
	
	/**
    * Constructs a row to insert into the build action popup window
    */
	private JPanel buildButtonRow() {
    	// if the clicked okay
    	okBtn = new JButton("Okay");
        okBtn.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			Environment.constructFragmentList();
    			
    			boolean all_good = true;
    			String msg_suffix = " already exists, is it okay to overwrite it?";
				String title = "Overwrite?";
				int optionType = JOptionPane.YES_NO_OPTION;
				int messageType = JOptionPane.QUESTION_MESSAGE;

    			int err_num = 0;
    			String errorMessages[] = {
    					"Unable to open parent directory: ",
    					"Unable to open parent directory: ",
    					"Unable to create temporary file: ",
    					"Unable to write to the temporary file: ",
    					"Unable to overwrite the file: ",
    					":("
    			};
    			
    			int NUM_CHECKED = 0;
    			boolean isChecked[] = new boolean[NUM_ITEMS];
    			if (isChecked[0] = checkBoxes[0].isSelected()) {
    				NUM_CHECKED++;
    			}
    			if (isChecked[1] = checkBoxes[1].isSelected()) {
    				NUM_CHECKED++;
    			}
    			if (isChecked[2] = checkBoxes[2].isSelected()) {
    				NUM_CHECKED++;
    			}
    			
    			// hide the window
    			dialog.setVisible(false);
    			Environment.parentFrame.requestFocus();

    			// if no boxes are checked, no point processing anything...
    			if (NUM_CHECKED == 0) {
    				return;
    			}
    			
    			// grab the directory to save to
				File parent = directoryCheck(new File(locationText.getText()));
				if (!(all_good &= parent != null)) {
    				err_num = 0;
					errorMessages[err_num] += locationText.getText();
    			}
				
				// create all of the File objects
				File theFiles[] = createFileObjects(parent);
    			for(int i = 0; i < NUM_ITEMS; i++) {
    				all_good &= (theFiles[i] != null);
    			}
    			if (!all_good) {
    				err_num = 1;
					errorMessages[err_num] += parent.getAbsolutePath();
    			}
    			
    			for(int i = 0; i < NUM_ITEMS; i++) {
    				if (!isChecked[i] || !all_good) {
    					continue;
    				}
    				
    				// create a temporary file to write to
    				File temporaryFile = null;
    				try {
    					temporaryFile = File.createTempFile("tmp", theFiles[i].getName(), parent);
    				} catch (IOException ex) {
    					err_num = 2;
    					errorMessages[err_num] += ex.getMessage();
						all_good &= false;
    				}
				
    				// write to the temporary file
    				if (all_good) {
    					switch (i) {
    						case 0:
    							all_good &= cppAction(temporaryFile);
    							break;
    							
    						case 1:
    							all_good &= grammarPhase1Action(temporaryFile);
    							break;
    							
    						case 2:
    							all_good &= grammarPhase2Action(temporaryFile);
    							break;
    					}
    					if (!all_good) {
    						err_num = 3;
    						errorMessages[err_num] += temporaryFile.getAbsolutePath();
    					}
    				}
    				
    				// check for existing files and query and delete if needed
					if (all_good && theFiles[i].exists()) {
						if (JOptionPane.showConfirmDialog(null, theFiles[i].getName() + msg_suffix, title, optionType, messageType) == JOptionPane.YES_OPTION) {
							if (all_good &= theFiles[i].delete()) {
								all_good &= temporaryFile.renameTo(theFiles[i]);
							}
						} else {
							all_good &= false;
						}
					} else {
						all_good &= temporaryFile.renameTo(theFiles[i]);
					}
					if (!all_good) {
						temporaryFile.delete();
						err_num = 4;
						errorMessages[err_num] += theFiles[i].getAbsolutePath();
	    			}
    			}
    			
				// OKAY, so did something go wrong or not?
    			if (!all_good) {
    				String msg[] = {
    						"An error occurred:",
    						"\"" + errorMessages[err_num] + "\"",
    						"Build halted."
    				};
    				JOptionPane.showMessageDialog(dialog, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
    			} else {
    				// if nothing went wrong, then popup a message saying what was built and then finish!
    				String msg[] = new String[NUM_CHECKED + 1];
    				msg[0] = "Successfully Built:";
    				for(int i = 0, j = 1; i < NUM_ITEMS; i++) {
    					if (isChecked[i]) {
    						msg[j++] = "-\"" + theFiles[i].getAbsolutePath() + "\"";
    					}
    				}
    				
    				Environment.buildUpToDate(true);
    				Environment.lastBuildFolder = parent;
    				
    				JOptionPane.showMessageDialog(dialog, msg, "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
    			}
    		}
    	});
    	
    	JButton cancelBtn = new JButton("Cancel");
    	cancelBtn.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			dialog.setVisible(false);
    			Environment.parentFrame.requestFocus();
    		}
    	});
    	
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
    	
		row.add(Box.createHorizontalGlue());
		row.add(okBtn);
		row.add(Box.createRigidArea(horizontalGlueSize));
    	row.add(cancelBtn);
    	row.add(Box.createRigidArea(horizontalGlueSize));
    	
    	return row;
	}
	
	/**
	* Clears all data from the given file (convenience method)
	* @param filename - the string filename to wipe
	* @return true on success, false otherwise
	*/
	public static boolean wipeOutputFile(String filename) {
		return wipeOutputFile(new File(filename));
	}
	
	/**
	* Clears all data from the given file
	* @param theFile - the File to wipe
	* @return true on success, false otherwise
	*/
	public static boolean wipeOutputFile(File theFile) {
		try {
			if (theFile.exists()) {
				if (!theFile.delete()) {
    				throw new IOException();
				}
			}
			if (!theFile.createNewFile()) {
				throw new IOException();
			}
		} catch (IOException ex) {
			String msg[] = {
					"An error occurred:",
					"Unable to clear temporary output file: \"" + theFile.getAbsolutePath() + "\""
			};
			JOptionPane.showMessageDialog(null, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	/**
    * Asks the user where they'd like to save something (files only) - convenience method
    * @param fileFilterName - the description for the file filter
    * @param fileFilterExtension - the extension for the file filter
    * @param windowTitle - the label for the popup window
    * @param buttonLabel - the label to go on the "save" button
    * @return the file that was selected.
    */
	public static File popupSaveDialog(File location, String fileFilterName, String fileFilterExtension, String windowTitle, String buttonLabel) {
		return popupSaveDialog(location, fileFilterName, fileFilterExtension, windowTitle, buttonLabel, false);
	}
	
	/**
    * Asks the user where they'd like to save something - convenience method
    * @param fileFilterName - the description for the file filter
    * @param fileFilterExtension - the extension for the file filter
    * @param windowTitle - the label for the popup window
    * @param buttonLabel - the label to go on the "save" button
    * @param foldersOnly - true if they can only select folders, false otherwise
    * @return the file (or folder) that was selected. If foldersOnly is true, and a file is selected, then that file's parent folder is returned
    */
	public static File popupSaveDialog(File location, String fileFilterName, String fileFilterExtension, String windowTitle, String buttonLabel, boolean foldersOnly) {
		return popupDialog(true, location, fileFilterName, fileFilterExtension, windowTitle, buttonLabel, foldersOnly);
	}
	
	/**
    * Asks the user where they'd like to save something - convenience method
    * @param save - true to popup a save dialog, false for open dialog
    * @param fileFilterName - the description for the file filter
    * @param fileFilterExtension - the extension for the file filter
    * @param windowTitle - the label for the popup window
    * @param buttonLabel - the label to go on the "save" button
    * @param foldersOnly - true if they can only select folders, false otherwise
    * @return the file (or folder) that was selected. If foldersOnly is true, and a file is selected, then that file's parent folder is returned
    */
	public static File popupDialog(boolean save, File location, String fileFilterName, String fileFilterExtension, String windowTitle, String buttonLabel, boolean foldersOnly) {
		//make a file chooser
		chooser = new JFileChooser();
        
        //we don't want to select multiple files
        chooser.setMultiSelectionEnabled(false);
        
        if (fileFilterName != null && fileFilterExtension != null) {
    		//we only want to see .txt files
    		FileNameExtensionFilter filter = new FileNameExtensionFilter(fileFilterName, fileFilterExtension);
            chooser.setFileFilter(filter);
        }
        
        if (foldersOnly) {
        	//we don't care what they select (we'll filter it later)
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
        
        //open it to the last opened directory
        chooser.setCurrentDirectory(location);
        
        //some un-needed fanciness
        chooser.setApproveButtonText(buttonLabel);
        chooser.setDialogTitle(windowTitle);
        
        //file finding time!
        int choice = JFileChooser.CANCEL_OPTION;
        if (save) {
        	choice = chooser.showSaveDialog(null);
        } else {
        	choice = chooser.showOpenDialog(null);
        }
        if(choice == JFileChooser.APPROVE_OPTION) {
        	Environment.lastOpenLocation = chooser.getCurrentDirectory();
            
            if (foldersOnly) {
            	return directoryCheck(chooser.getSelectedFile());
            } else {
            	return chooser.getSelectedFile();
            }
        } else {
            return null;
        }
	}
	
	/**
	* Checks to make sure that theFile is a directory, returns a parent if it is not or it does not exist
	* @param theFile the File object to check
	* @return the valid directory
	*/
	private static File directoryCheck(File theFile) {
		theFile = theFile.getAbsoluteFile();
        if (theFile.isDirectory() && theFile.exists()) {
        	return theFile;
        } else {
        	if (theFile.getParentFile() == null) {
        		return Environment.lastOpenLocation;
        	} else {
        		return theFile.getParentFile();
        	}
        }
	}
	
	/**
    * Used to convert and save the contentPane in the C++ format used by the GA
    * @return true on successful save, false otherwise
    */
    public boolean cppAction(File theFile) {
		if (theFile == null) {
		 	theFile = popupSaveDialog(Environment.lastOpenLocation, "C Source Code File (*.c)", "c", "Select Where to Save the TreeData C File", "Save Tree", true);
		}
		 
		if (theFile != null) {
	    	// construct the cpp code file
			int counter = Fragment.countNumFragments(Environment.fragmentList);
			String cpp_code_string = constructCPPCode(Environment.fragmentList, counter);
			
			// make sure it ends in .cpp
			if (!theFile.getName().endsWith(".c")) {
				theFile = new File(theFile.getParentFile().getAbsolutePath() + File.separator + theFile.getName() + ".c");
		    }
		     
		    return saveToFile(theFile, cpp_code_string);
		 }
		 
		 return false;
	}
     
    /**
    * Used to convert and save the contentPane as the grammar used in phase 1 of the GA
    * @return true on successful save, false otherwise
    */
    public boolean grammarPhase1Action(File theFile) {
        if (theFile == null) {
        	theFile = popupSaveDialog(Environment.lastOpenLocation, "Backus Naur Grammar File (*.bnf)", "bnf", "Select Where to Save the Phase 1 Grammar File", "Save Grammar");
        }
        
        if (theFile != null) {
            // construct the grammar code file
            String grammar_string;
            if ( (grammar_string = constructPhase1GrammarCode(Environment.fragmentList)) == null) {
        		return false;
    		}
            
            // make sure it ends in .bnf
            if (!theFile.getName().endsWith(".bnf")) {
                theFile = new File(theFile.getParentFile().getAbsolutePath() + File.separator + theFile.getName() + ".bnf");
            }
            
            return saveToFile(theFile, grammar_string);
        }
        
        return false;
    }
     
    /**
    * Used to convert and save the contentPane as the grammar used in phase 2 of the GA
    * @return true on successful save, false otherwise
    */
    public boolean grammarPhase2Action(File theFile) {
    	if (theFile == null) {
        	theFile = popupSaveDialog(Environment.lastOpenLocation, "Backus Naur Grammar File (*.bnf)", "bnf", "Select Where to Save the Phase 2 Grammar File", "Save Grammar");
    	}
    	
        if (theFile != null) {
            // construct the cpp code file
            String grammar_string;
            if ( (grammar_string = constructPhase2GrammarCode(Environment.fragmentList)) == null) {
        		return false;
    		}
            
            // make sure it ends in .bnf
            if (!theFile.getName().endsWith(".bnf")) {
                theFile = new File(theFile.getParentFile().getAbsolutePath() + File.separator + theFile.getName() + ".bnf");
            }
            
            return saveToFile(theFile, grammar_string);
        }
        
        return false;
    }
				
    /**
    * Saves the fileText to theFile via a temporary file
    * @param theFile - a File object representing the destination file
    * @param fileText - the text to write to the file
    * @return true if saved successfully, false otherwise
    */
    public static boolean saveToFile(File theFile, String fileText) {
       File temporaryFile = null;
       boolean success = false;
       
       String theFileName = theFile.getName();
       File pathFile = theFile.getParentFile();
       String path = pathFile.getAbsolutePath();
        
       // We write to a temporary file so we don't overwrite if an error occurs
       temporaryFile = new File(path + File.separator + "tmpfile.tmp");
       try {
           // Write the CPP file text out
           BufferedWriter out = new BufferedWriter(new FileWriter(temporaryFile));
           out.write(fileText);
           out.close();
       
           //rename the temporary file because all is well
           if (theFile.exists()) {
               if (theFile.delete()) {
                   if (!temporaryFile.renameTo(theFile)) {
                       throw new IOException("Unable to overwrite the selected file.");
                   }
               } else {
                   throw new IOException("Unable to overwrite the selected file.");
               }
           } else {
               temporaryFile.renameTo(theFile);
           }
           
           success = true;
       } catch (Exception e) {
           String message[] = {"An error has occurred whilst trying to save " + theFileName,
                                e.getMessage()};
           JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
           
           success = false;
       }
		
       if (temporaryFile != null) {
    	   temporaryFile.delete();
       }
         
       return success;
    }
     
    /**
    * Constructs a string containing the C++ code for use by the GA
    * @param list - the organised fragment list
    * @param counter - the number of non-leaf fragments
    * @return a String representing the code
    */
    private String constructCPPCode(Fragment[] list, int counter) {
    	String fileText = "";
		
		// now we make the file
	    fileText += "#include \"genomeHelper.h\"" + "\n";
	    fileText += "\n";
	    fileText += "int * params;" + "\n";
	    fileText += "int ** children;" + "\n";
	    fileText += "int * output;" + "\n";
	    fileText += "int num_fragments = " + counter + ";" + "\n";
	    fileText += "\n";
	    fileText += "void treeData() {" + "\n";

	    // Print the declaration for the output list
	    fileText += "\t" + "params = (int*)b_allocate(sizeof(int), num_fragments);" + "\n";
	    fileText += "\t" + "children = (int**)b_allocate(sizeof(int*), num_fragments);" + "\n";
	    fileText += "\t" + "output = (int*)b_allocate(sizeof(int), num_fragments);" + "\n";
	    fileText += "\n";
	    fileText += "\t" + "int i = 0;" + "\n";
	    fileText += "\n";
	    
	    // Print the output list
		for(Fragment f : list) {
			if (f != null) {
				if (f.numChildren() != 0) {
					fileText += "\t" + "params[i] = " + f.value + ";" + "\n";
					fileText += "\t" + "children[i] = (int*)b_allocate(sizeof(int), " + f.numChildren() + ");" + "\n";
					for(int i = 0; i < f.numChildren(); i++) {
						fileText += "\t" + "children[i][" + i + "] = " + list[f.childrenIDs.get(i)].value + ";" + "\n";
					}
					fileText += "\t" + "output[i++] = ";
					if (f.hasOutput) {
						fileText += f.outputValue;
					} else {
						fileText += -32767; // this is my "INT_MIN" constant for invalid values...
					}
					fileText += ";" + "\n";
					fileText += "\n";
				} else if (f.hasOutput) {
					fileText += "\t" + "params[i] = " + f.value + ";" + "\n";
					fileText += "\t" + "children[i] = (int*)b_allocate(sizeof(int), 0);" + "\n";
					fileText += "\t" + "output[i++] = " + f.outputValue + ";" + "\n";
					fileText += "\n";
                }
			}
		}
		
		fileText += "}" + "\n";
			
		return fileText;
    }
     
    /**
    * Constructs a string containing the grammar file for phase 1 of the GA
    * @param list - the organised fragment list
    * @return a String representing the grammar
    */
    private String constructPhase1GrammarCode(Fragment[] list) {
    	int needLoops = 0;
    	
    	// count the number of children per parent
    	int max = Integer.MIN_VALUE;
    	int min = Integer.MAX_VALUE;
    	int avg = 0;
    	int count = 0;
		for(Fragment f : list) {
		    if (f != null) {
		        max = Math.max(max, f.numChildren());
		        min = Math.min(min, f.numChildren());
		        avg += f.numChildren();
		        if (f.numChildren() != 0) {
			    	count++;
		        }
		    }
		}
		avg = avg / count;
		
		// check if there are inconsistencies and ask for confirmation
		if (avg != max || (avg != min && min != 0) || (min != max && min != 0)) {
			String message[] = {"I have detected that there are inconsistent numbers of recursive calls!", "This may mean that we need loop constructs."};
			String selects[] = {"It shouldn't need loops, don't evolve them.", "I didn't have all the child values, it may need loops.", "That was intentional, it needs loops.", "I made a mistake, let me fix it!"};
			String initial = "It shouldn't need loops, don't evolve them.";
            Object o = JOptionPane.showInputDialog( null,
								                    message,
								                    "Question!",
								                    JOptionPane.QUESTION_MESSAGE,
								                    null,
								                    selects,
								                    initial);
            if (o != null) {
                if (o instanceof String) {
                    if (selects[0].equals((String)o)) {
                    	needLoops = 0; // doesn't need loops
                    } else if (selects[1].equals((String)o)) {
                    	needLoops = 1; // might need loops, try for both
                    } else if (selects[2].equals((String)o)) {
                    	needLoops = 2; // definitely needs loops, don't do standard
                    } else if (selects[3].equals((String)o)) {
                    	return null;
                    }
                }
            }
        }
        
		// build the grammar
		Grammar gram = new Grammar();
		
        int index = gram.addNode("expr_root");
		if (needLoops == 0 || needLoops == 1) {
			String expr[] = {"doMaths(<var>, <digit>, <op>)", "doMaths(<digit>, <var>, <op>)"};
			gram.addExpressionToNode(index, expr);
		}
        
		if (needLoops == 1 || needLoops == 2) {
			gram.addExpressionToNode(index, "<loop_expr>");
			
			String loop_expr[] = {"/**/(<var> \"<\" i)/**//**/doMaths(<var>, i, '-')/**/", "/**/<guard>/**//**/doMaths(i, <var>, <loop_op>)/**/", "/**/<guard>/**//**/doMaths(<var>, i, <loop_op>)/**/"};
			gram.addNode("loop_expr", loop_expr);
			
			String loop_op[] = {"'*'", "'+'", "'/'"};
			gram.addNode("loop_op", loop_op);
			
			String guard[] = {"(doMaths(<var>, i, '%') == 0)", "(<var> \"<\" i)"/*, "(<var> \">\" i)"*/};
			gram.addNode("guard", guard);
		}
		
		if (needLoops == 0 || needLoops == 1) {
			String ops[]            = {"'-'", "'*'", "'+'"};
			gram.addNode("op", ops);
		
			String digit[]          = {"1", "2", "<big_digit>"};
			gram.addNode("digit", digit);
			
			String big_digit[]      = {"3", "4", "5", "<bigger_digit>"};
			gram.addNode("big_digit", big_digit);
			
			String bigger_digit[]   = {"6", "7", "<huge_digit>"};
			gram.addNode("bigger_digit", bigger_digit);
			
			String huge_digit[]     = {"8", "9"};
			gram.addNode("huge_digit", huge_digit);
		}
		
		String var[]     = {"x"};
		gram.addNode("var", var);
		
		return gram.toString();
    }
     
    /**
    * Constructs a string containing the STANDARD STRUCTURE grammar file for phase 2 of the GA
    * @param list - the organised fragment list
    * @return a String representing the grammar
    */
    private String constructPhase2GrammarCode(Fragment[] list) {
    	// count the number of children per parent
    	int maxNumChildren = -1;
		for(Fragment f : list) {
		    if (f != null) {
		        maxNumChildren = Math.max(maxNumChildren, f.numChildren());
		    }
		}
        
		// ask the user if there could be multiple base cases
        int numBaseCases = maxNumChildren;
        if (maxNumChildren > 1) {
            String message[] = {"I have detected that there might be multiple base cases!", "I think there are this many - correct me if I'm wrong!"};
            Object o = JOptionPane.showInputDialog(null,
                                                   message,
                                                   "Question!",
                                                   JOptionPane.QUESTION_MESSAGE,
                                                   null,
                                                   null,
                                                   maxNumChildren);
            if (o != null) {
                if (o instanceof String) {
                    try {
                        numBaseCases = Integer.parseInt((String)o);
                    } catch (NumberFormatException e) {
                    	return null;
                    }
                } else {
                	return null;
                }
            } else {
            	return null;
            }
        }
		
        // build the grammar
		Grammar gram = new Grammar();
		
		int index = gram.addNode("expr_root");
		String str = "";
		// there should be as many base cases as required...
		for (int i = 0; i < numBaseCases; i++) {
			str += "if (<var> \"<\" <digit>) {\\n\\\n";
			str += " return <lit>;\\n\\\n";
			str += "} else ";
		}
		str += "{\\n\\\n";
		str += " return ";
		// there should be as many recursive calls as there are children
		for (int i = 0; i < maxNumChildren; i++) {
			int idx = i+1;
		    if (i != 0) {
		    	str += " <plain_op> ";
		    }
			str += "<expr" + idx + ">";
		}
		str += ";\\n\\\n";
		str += "}";
		gram.addExpressionToNode(index, str);

		// put together the recursive parameter nodes
		for (int i = 0; i < maxNumChildren; i++) {
			int idx = i+1;
			String exprs[]      = {"<rec" + idx + ">", "doMaths(<rec" + idx + ">, <lit>, <op>)", "doMaths(<lit>, <rec" + idx + ">, <op>)"};
			gram.addNode("expr" + idx, exprs);
			
			gram.addNode("rec" + idx, "recurse(param(<var>, " + idx + "))");
		}
		
		// the maths operators
		gram.addNode("op", "'<plain_op>'");
		String ops[]            = {"-", "*", "+"};
		gram.addNode("plain_op", ops);
		
		// lits (vars or numbers)
		String lit[]			= {"<digit>", "<var>"};
		gram.addNode("lit", lit);
		
		// all of the digits
		String digit[]          = {"1", "2", "<big_digit>"};
		gram.addNode("digit", digit);
		String big_digit[]      = {"3", "4", "5", "<bigger_digit>"};
		gram.addNode("big_digit", big_digit);
		String bigger_digit[]   = {"6", "7", "<huge_digit>"};
		gram.addNode("bigger_digit", bigger_digit);
		String huge_digit[]     = {"8", "9"};
		gram.addNode("huge_digit", huge_digit);
		
		// vars - just one for now.. can add more later!
		String var[]     = {"x"};
		gram.addNode("var", var);
		
		return gram.toString();
    }
    
    /**
    * Constructs a string containing the LOOP STRUCTURE grammar file for phase 2 of the GA
    * @param str - the best individual from phase 1
    * @return a String representing the grammar
    */
    public static String constructPhase2GrammarCode(String str) {
    	// split out the items we want
		int loc1 = str.indexOf("/**/");
		int loc2 = str.indexOf("/**//**/");
		int loc3 = str.indexOf("/**/", loc2+8);
    	String guard   = str.substring(loc1+4, loc2);
    	String trans_i = str.substring(loc2+8, loc3);
		
    	// build the grammar
    	Grammar gram = null;
		
		gram = new Grammar();
		
		String expr = "\\\n";
		expr += "#define op <mrrc_op>\\n\\\n";
		expr += "int i, result;\\n\\\n";
		expr += "\\n\\\n";
		expr += "if (<pred>) {\\n\\\n";
		expr += "    return <ret>;\\n\\\n";
		expr += "}\\n\\\n";
		expr += "\\n\\\n";
		expr += "result = base_op_val;\\n\\\n";
		expr += "for (i = <rl>; i \"<\" <ru>; i++) {\\n\\\n";
		expr += "    if (" + guard + ") {\\n\\\n";
		expr += "        result = result op mrrc(" + trans_i + ", i);\\n\\\n";
		expr += "    }\\n\\\n";
		expr += "}\\n\\\n";
		expr += "\\n\\\n";
		expr += "return result;\\n";
		gram.addNode("expr_root", expr);
		
		String mrrc_op[] = {"+ \\n#define base_op_val 0", "- \\n#define base_op_val 0", "* \\n#define base_op_val 1"};
		gram.addNode("mrrc_op", mrrc_op);
		
		String pred[] = {"<var> \"<\" <digit>", "<var> \">\" <digit>"};
		gram.addNode("pred", pred);
		
		String ret[] = {"<lit>"};
		gram.addNode("ret", ret);
		
		String rl[] = {"c", "<lit>"};
		gram.addNode("rl", rl);
		
		String ru[] = {"(<var> + <digit>)", "(<var> - <digit>)", "(<digit> - <var>)", "<var>"};
		gram.addNode("ru", ru);
		
		//String guard[] = {"(<var> % i == 0)", "(<var> \">\" i)", "(<var> \"<\" i)"};
		//gram.addNode("guard", guard);
		
		// lits (vars or numbers)
		String lit[]			= {"<digit>", "<var>"};
		gram.addNode("lit", lit);
		
		// all of the digits
		String digit[]          = {"1", "2", "<big_digit>"};
		gram.addNode("digit", digit);
		String big_digit[]      = {"3", "4", "5", "<bigger_digit>"};
		gram.addNode("big_digit", big_digit);
		String bigger_digit[]   = {"6", "7", "<huge_digit>"};
		gram.addNode("bigger_digit", bigger_digit);
		String huge_digit[]     = {"8", "9"};
		gram.addNode("huge_digit", huge_digit);
		
		// vars - just one for now.. can add more later!
		String var[]     = {"x"};
		gram.addNode("var", var);
		
		return gram.toString();
    }
}
