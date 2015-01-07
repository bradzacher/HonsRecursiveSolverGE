import java.awt.Component;
import java.io.File;

/**
 * Stores all global state for the system.
 */
public class Environment {
	/**
	 * 
	 */
	public static boolean GUI = true;

	/**
	 * The latest built list of fragments
	 */
	public static Fragment fragmentList[];

	// important awt pointers
	/**
	 * The BuilderWindow which holds everything
	 */
	public static BuilderWindow parentFrame;
	/**
	 * The current content pane
	 */
	public static BuilderWindowPanel contentPane;
	
	// important file pointers
	/**
	 * The last location that the user opened from
	 */
	public static File lastOpenLocation = new File(".");
	/**
	 * The file which was last opened - used for the save without dailog
	 */
	public static File currentlyOpenedFile = null;
	/**
	 * the location the fragments were lasts built to
	 */
	public static File lastBuildFolder = null;
	
	// tracks the status of saves
	/**
	 * true if the window has been saved since a change
	 */
	private static boolean save_upToDate = true;
	/**
	 * true if the window has been built since a change
	 */
	private static boolean build_upToDate = true;
	

	/**
 	* Update the frame as to the freshness of its content pane
 	* @param val true if the panel has been changed, false otherwise
 	*/
	public static void panelChanged(boolean val) {
		saveUpToDate(!val);
		buildUpToDate(!val);
	}
	
	/**
	* Tells the frame if the content pane has been saved
	* @param val - true if saved, false otherwise
	*/
	public static void saveUpToDate(boolean val) {
		save_upToDate = val;
	}
	
	/**
	* Tells the frame if the content pane has been built
	* @param val - true if built, false otherwise
	*/
	public static void buildUpToDate(boolean val) {
		build_upToDate = val;
	}
	
	/**
	* Checks if the pane has been saved since last change
	* @return true if has, false otherwise
	*/
	public static boolean isSaveUpToDate() {
		return save_upToDate;
	}
	
	/**
	* Checks if the pane has been built since last change
	* @return true if has, false otherwise
	*/
	public static boolean isBuildUpToDate() {
		return build_upToDate;
	}
	
    /**
    * Constructs the list of tree Fragments from the opened tree file, stores it in Environment.fragmentList
    */
    public static void constructFragmentList() {
		// build the list of parent->children relationships
		Component children[] = Environment.contentPane.getComponents();
		fragmentList = new Fragment[FragmentParentIcon.ID_COUNTER];
		
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof LinkIcon) {
				LinkIcon l = (LinkIcon)children[i];
				fragmentList[l.start.ID].addChild(l.finish.ID);
			} else if (children[i] instanceof FragmentParentIcon) {
				FragmentParentIcon f = (FragmentParentIcon)children[i];
				fragmentList[f.ID] = new Fragment(f.value, f.outputValue, f.hasOutput);
			}
		}
		
		fragmentList = Fragment.sortFragmentList(fragmentList);
    }
}
