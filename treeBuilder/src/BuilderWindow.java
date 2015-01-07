import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
* Custom JFrame used to house the JMenu and JPanel for displaying the Call Tree Builder GUI
*/
public class BuilderWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The JFileChooser which is frequently used
	 */
    private JFileChooser chooser;
	
	private final Dimension MAX_DIMENSION = new Dimension(800, 800);
	
	
	/**
    * Default Constructor
    */
	public BuilderWindow() {
		super("Call Tree Builder");
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// position in the middle of the screen
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		this.setLocation(p.x - (MAX_DIMENSION.width / 2), p.y - (MAX_DIMENSION.width / 2));
        
        // setup an empty content panel
		createBlankPane();
        this.setLayout(null);
		
        // add the menu bar
		this.setJMenuBar(new MenuBar(this));
		
	    //Show the window.
	    this.pack();
	    this.validate();
	    this.setVisible(true);
        
        // Make sure that the fragments are correctly positioned after a resize
        this.addComponentListener(new ComponentListener() {  
            // This method is called after the component's size changes
            public void componentResized(ComponentEvent e) {
                Environment.contentPane.reposition();
            }
            
            public void componentHidden(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
        });
	}
	
	/**
	* Creates a blank panel, overwrites the current content pane
	*/
	private void createBlankPane() {
		BuilderWindowPanel pane = new BuilderWindowPanel();
		
		pane.setPreferredSize(MAX_DIMENSION);
		pane.setMinimumSize(MAX_DIMENSION);
		pane.setMaximumSize(MAX_DIMENSION);
		pane.setLayout(null);
		
		Environment.panelChanged(false);
		Environment.currentlyOpenedFile = null;
		Environment.lastBuildFolder = null;
		
		this.setContentPane(pane);
		Environment.contentPane = pane;
		Environment.parentFrame = this;
		
		this.pack();
		this.validate();
	}
	
	/**
	 * The driver
	 * @param args
	 */
 	public static void main(String args[]) {
		new BuilderWindow();
	}
	
 	/**
 	 * Saves to the currently open file (no dialog)
 	 */
	public void saveAction() {
		if (!Environment.isSaveUpToDate()) {
    		saveAsAction(Environment.currentlyOpenedFile);
    	}
	}
	
	/**
	 * prompts the system to save the content to a .tree file
	 * @param theFile null if want to ask the user where to save, else the file to save to
	 * @return true on success, false otherwise
	 */
	public boolean saveAsAction(File theFile) {
		boolean success = false;
        
		//A temporary file so we don't overwrite if an error occurred
		File temporaryFile = null;
		
		if (theFile == null) {
			theFile = TreeBuilder.popupSaveDialog(Environment.lastOpenLocation, "Unbuilt Trees (*.tree)", "tree", "Select Where to Save the Tree File", "Save Tree");
		}
		JFileChooser chooser = TreeBuilder.chooser;
		
        if(theFile != null) {
			String theFileName = theFile.getName();
			File pathFile = theFile.getParentFile();
			String path = pathFile.getAbsolutePath();
			
        	if (chooser != null) {
        		// if the user selected the "All Files" filter, we let them use whatever extension they want
				if (chooser.getFileFilter().getClass() != chooser.getAcceptAllFileFilter().getClass()) {
					// if they didn't provide an extension and had the ".tree" filter
					if (theFileName.lastIndexOf('.') == -1) {
						theFile = new File(path + File.separator + theFileName + ".tree");
					}
				}
        	}
			try {
				temporaryFile = new File(path + File.separator + "tmptree.tmp");
				
			    // Serialize to a file
			    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(temporaryFile));
			    out.writeObject(Environment.contentPane);
			    out.close();
			    
			    //rename the temporary file
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
                Environment.saveUpToDate(true);
			} catch (Exception e) {
        		String message[] = {"An error has occurred whilst trying to save " + theFile,
						e.getMessage()};
    			JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
				
        		success = false;
			}
        }
		
		if (temporaryFile != null) {
			temporaryFile.delete();
		}
		return success;
	}
	
    /**
    * Custom JMenuBar
    */
 	private class MenuBar extends JMenuBar {
		private static final long serialVersionUID = 1L;
		
        /**
        * Default constructor
        * @param parentFrame - the BuilderWindow panel which this menu bar will service
        */
		private MenuBar(BuilderWindow parentFrame) {
			super();
			
			super.add(new FileMenu());
			super.add(new EditMenu());
			super.add(new ProjectMenu());
		}
	}
	
	/**
    * Custom JMenu #1
    */
    private class FileMenu extends JMenu {
		private static final long serialVersionUID = 1L;
		private JMenuItem newOpt;
		private JMenuItem open;
		private JMenuItem save;
		private JMenuItem saveAs;
		private JMenuItem quit;
		
        /**
        * Default Constructor
        * @param parentFrame - the BuilderWindow which will be the root parent of the menu bar
        */
		private FileMenu() {
			super("File");
			
			//add all the menu items
			super.add(newOpt = new JMenuItem("New..."));
			super.add(open = new JMenuItem("Open..."));
			super.add(save = new JMenuItem("Save..."));
			super.add(saveAs = new JMenuItem("Save as..."));
			super.add(quit = new JMenuItem("Quit!"));
			
			//setup the keyboard shortcuts
			newOpt.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
			open.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
			save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
			saveAs.setAccelerator(KeyStroke.getKeyStroke("ctrl shift S"));
			quit.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
			
			//setup all the listeners on the menu items
			this.setupListeners();
		}
		
		/**
        * Attach listeners to each of the menu options
        */
        private void setupListeners() {
        	newOpt.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					newAction();
				}
			});
        	
			open.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openAction();
				}
			});
			
			save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveAction();
				}
			});
			
			saveAs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveAsAction();
				}
			});
			
			quit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					quitAction();
				}
			});
		}

        /**
        * Used to create a fresh tree
        */
        private void newAction() {
        	if (!Environment.isSaveUpToDate()) {
        		String msg[] = {
        				"Create a new tree?",
        				"All unsaved changes will be lost!"
        		};
	        	if (JOptionPane.showConfirmDialog(null, msg, "Create a new tree", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
	        		return;
	        	}
        	}
        	
        	createBlankPane();
        	Environment.parentFrame.repaint();
        }
        
		/**
        * Used to trigger the prompts to open a *.tree file
        */
        private void openAction() {
			//make a file chooser
			chooser = new JFileChooser();
			
			//we only want to see .tree files
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Unbuilt Trees (*.tree)", "tree");
	        chooser.setFileFilter(filter);
	        
	        //we don't want to select multiple files
	        chooser.setMultiSelectionEnabled(false);
	        
	        //open it to the last opened directory
	        chooser.setCurrentDirectory(Environment.lastOpenLocation);
	        
	        //some un-needed fanciness
	        chooser.setApproveButtonText("Open Selected Tree");
            chooser.setDialogTitle("Select a Tree File to Open");
	        
	        //file finding time!
	        int choice = chooser.showOpenDialog(null);
	        if(choice == JFileChooser.APPROVE_OPTION) { //if they pressed any other option, we don't care
	        	BuilderWindowPanel newContentPane = null;
	        	try {
	        	    // Deserialize from a file
	        	    ObjectInputStream in = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()));
	        	    // Deserialize the object
	        	    newContentPane = (BuilderWindowPanel) in.readObject();
	        	    in.close();
	        	} catch (Exception e) {
	        		String message[] = {"An error has occurred whilst trying to load " + chooser.getSelectedFile().getName(),
	        							e.getMessage()};
	        		JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
	        		return;
	        	}
        	    
        	    // overwrite the old panel with the new one
	        	Environment.parentFrame.setContentPane(newContentPane);
	        	Environment.contentPane = newContentPane;
	        	Environment.contentPane.setLayout(null);
                
	        	Environment.currentlyOpenedFile = chooser.getSelectedFile();
	        	Environment.lastBuildFolder = null;

	        	Environment.parentFrame.pack();
	        	Environment.parentFrame.validate();
        	    
        	    // This is a bit of a kludge to make sure that everything is positioned correctly when it gets unserialized
	        	Environment.contentPane.reposition();
        		
        	    // update the pane change status
	        	Environment.saveUpToDate(true);
	        	Environment.buildUpToDate(false);
	        	
	        	//save the current folder as the next folder to open from
	        	Environment.lastOpenLocation = chooser.getCurrentDirectory();
	        }
		}
		
        /**
        * Used to save the current contentPane as a *.tree file
        */
        private void saveAction() {
        	Environment.parentFrame.saveAction();
        }
        
		/**
        * Used to save the current contentPane as a *.tree file - convenience method
        * @return true on successful save, false otherwise
        */
        private boolean saveAsAction() {
        	return saveAsAction(null);
        }
        
        /**
        * Used to save the current contentPane as a *.tree file
        * @param theFile - the file to save to. leave null if a new file is required
        * @return true on successful save, false otherwise
        */
        private boolean saveAsAction(File theFile) {
            return Environment.parentFrame.saveAsAction(theFile);
		}
              
        /**
        * Used to quit gracefully, asks the user if they wish to save first
        */
        public void quitAction() {
			int response = JOptionPane.showConfirmDialog(null, "Do you want to save the tree before quitting?", "Save Tree?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				//if they do want to save
				if (!this.saveAsAction()) {
					//if we get false, cancel was pressed or something went wrong, so don't quit
					return;
				}
			} else if (response == JOptionPane.CANCEL_OPTION) {
				// if cancel is pressed - we don't want to close
				return;
			}
			
			System.exit(0);
		}
	}
	
	/**
    * Custom JMenu #2
    */
    private class EditMenu extends JMenu {
		private static final long serialVersionUID = 1L;
		
		private JMenuItem newNode;
		private JMenuItem deleteNode;
		private JMenuItem selectAll;
		private JMenuItem deselectAll;
		
		/**
        * Default Constructor
        * @param parentFrame - the BuilderWindow which will be the root parent of the menu bar
        */
		private EditMenu() {
			super("Edit");

			//add all the menu items
			super.add(newNode = new JMenuItem("New node..."));
			newNode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
			
			super.add(deleteNode = new JMenuItem("Delete node..."));
			deleteNode.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
			
			super.add(selectAll = new JMenuItem("Select all..."));
			selectAll.setAccelerator(KeyStroke.getKeyStroke("ctrl A"));
			
			super.add(deselectAll = new JMenuItem("Deselect all..."));
			deselectAll.setAccelerator(KeyStroke.getKeyStroke("ctrl D"));
			
			//setup all the listeners on the menu items
			this.setupListeners();
		}
		
		/**
        * Attach listeners to each of the menu options
        */
        private void setupListeners() {
			newNode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					newNodeAction();
				}
			});
			
			deleteNode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deleteNodeAction();
				}
			});
			
			selectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selectAllAction();
				}
			});
			
			deselectAll.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deselectAllAction();
				}
			});
		}
		
		/**
        * Used to add a new node to the contentPane
        */
        private void newNodeAction() {
			int v = 0;
			int o = 0;
			boolean hasOutput = false;
			String value;
			
			while (true) {
				value = JOptionPane.showInputDialog(null, "Please enter the integer value of the parameter for new node.", "Enter Parameter Value", JOptionPane.QUESTION_MESSAGE);
				if (value == null) {
					return;
				} else {
					try {
						v = Integer.parseInt(value);
						break;
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
			
			String message[] = {"Please enter the integer value of the output value of the new node.", "Leave blank if you do not know the value."};
			while (true) {
				value = JOptionPane.showInputDialog(null, message, "Enter Output Value", JOptionPane.QUESTION_MESSAGE);
				if (value == null) {
					return;
				} else {
					if (value.equals("")) {
						hasOutput = false;
					} else {
						try {
							o = Integer.parseInt(value);
							hasOutput = true;
						} catch (NumberFormatException e) {
							continue;
						}
					}
					break;
				}
			}
			
			if (hasOutput) {
				Environment.contentPane.add(new FragmentParentIcon(v, o));
			} else {
				Environment.contentPane.add(new FragmentParentIcon(v));
			}
			
			Environment.parentFrame.repaint();
		}
		
		/**
        * Used to delete all selected nodes and/or links from the content pane
        */
        private void deleteNodeAction() {
			LinkedList<Integer> selectedIcons = new LinkedList<Integer>();

			Component children[] = Environment.contentPane.getComponents();
			for (int i = 0; i < children.length; i++) {
				BuilderWindowIcon f = (BuilderWindowIcon) children[i];
				if (f.isSelected()) {
					selectedIcons.push(i);
				}
			}
			
			// if none selected, do nothing
			if (selectedIcons.size() == 0) {
				return;
			}
			
			// if more than 1 selected, ask first
			if (selectedIcons.size() > 1) {
				int choice = JOptionPane.showConfirmDialog(null, "Do you really want to delete multiple items?", "Delete?", JOptionPane.YES_NO_OPTION);
				if (choice != JOptionPane.YES_OPTION) {
					return;
				}
			}
			
			for (int i : selectedIcons) {
				if (children[i] instanceof FragmentParentIcon) {
					((FragmentParentIcon)children[i]).deleteMe();
				}
				Environment.contentPane.remove(i);
			}
			Environment.parentFrame.repaint();
		}
		
		/**
        * Selects all unselected nodes and/or links on the contentPane
        */
        private void selectAllAction() {
			for (Component c : Environment.contentPane.getComponents()) {
				BuilderWindowIcon b = (BuilderWindowIcon)c;
				if (!b.isSelected()) {
					b.toggleSelect();
				}
			}
			
			Environment.parentFrame.repaint();
		}
		
		/**
        * Deselects all selected nodes and/or links on the contentPane
        */
        private void deselectAllAction() {
			for (Component c : Environment.contentPane.getComponents()) {
				BuilderWindowIcon b = (BuilderWindowIcon)c;
				if (b.isSelected()) {
					b.toggleSelect();
				}
			}
			
			Environment.parentFrame.repaint();
		}
	}
    
    /**
    * Custom JMenu #3
    */
    private class ProjectMenu extends JMenu {
		private static final long serialVersionUID = 1L;

		private JMenuItem build;
		private JMenuItem run;
		
		private TreeBuilder builder;
		
        /**
        * Default Constructor
        * @param parentFrame - the BuilderWindow which will be the root parent of the menu bar
        */
		private ProjectMenu() {
			super("Project");
			
			//this.parentFrame = parentFrame;
			this.builder = new TreeBuilder();
			
			//add all the menu items
			super.add(build = new JMenuItem("Build all items..."));
			super.add(run = new JMenuItem("Generate solution..."));
			
			//setup the keyboard shortcuts
			build.setAccelerator(KeyStroke.getKeyStroke("ctrl B"));
			run.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
			
			//setup all the listeners on the menu items
			this.setupListeners();
		}
		
		/**
        * Attach listeners to each of the menu options
        */
        private void setupListeners() {
        	build.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					buildAction();
				}
			});
        	
        	run.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					runAction();
				}
			});
		}
		
        /**
        * Runs all of the builder functions sequentially (or until one fails)
        */
        private void buildAction() {
        	builder.popupDialog();
        }
        
        /**
        * Runs the GA to generate the solution
        */
        private void runAction() {
        	// make sure it's saved
        	if (!Environment.isSaveUpToDate()) {
        		String msg[] = {
        				"The current tree has not been saved!",
        				"The tree must be saved before a solution can be generated!"
        		};
        		
        		JOptionPane.showMessageDialog(null, msg, "Tree must be saved!", JOptionPane.WARNING_MESSAGE);
        		
        		Environment.parentFrame.saveAction();
        		
        		// if the save isn't up to date after popping up the window - something went wrong and don't continue!
        		if (!Environment.isSaveUpToDate()) {
        			return;
        		}
        	}
        	
        	// make sure it's built
        	if (!Environment.isBuildUpToDate()) {
        		String msg[] = {
        				"The current tree has not been built!",
        				"The tree bust be built before a solution can be generated!"
        		};

        		JOptionPane.showMessageDialog(null, msg, "Tree must be built!", JOptionPane.WARNING_MESSAGE);
        		
        		buildAction();
	        		
        		// if the build isn't up to date after popping up the window - something went wrong and don't continue!
        		if (!Environment.isBuildUpToDate()) {
        			return;
        		}
        	}

        	File parent = Environment.lastBuildFolder;
        	// this is the object which handles the creation of threads to do the processing on...
        	new RunHandler(Fragment.maxNumChildren(Environment.fragmentList), parent);
        	
        	//runner.phase1(Fragment.maxNumChildren(parentFrame.fragmentList), theFiles[0].getAbsolutePath(), theFiles[1].getAbsolutePath(), theFiles[3].getAbsolutePath());
        	
        	
        	
        	//String phase2result = RunHandler.phase2(fileNames2);*/
        }
    }
}