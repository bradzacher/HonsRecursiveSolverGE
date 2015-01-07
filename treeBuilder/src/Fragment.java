import java.util.LinkedList;
	
/**
* Convenience class for sorting out how the tree works
*/
public class Fragment {
	/**
	 * The input value (function parameter) this node represents
	 */
	public final int value;
	/**
	 * The output value (function return value) this node represents - only use if hasOutput is true
	 */
	public final int outputValue;
	/**
	 * True if the node was created with an output value, false otherwise
	 */
	public final boolean hasOutput;
	/**
	 * The list of children of this node
	 */
	public LinkedList<Integer> childrenIDs;
		
    /**
    * Default Constructor
    * @param value - the parameter value of this node
    * @param outputValue - the output value (if known) retrieved from running "value" through the recursive function
    * @param hasOutput - true if the outputValue is known, false otherwise
    */
	public Fragment(int value, int outputValue, boolean hasOutput) {
		this.value = value;
		this.outputValue = outputValue;
		this.hasOutput = hasOutput;
		this.childrenIDs = new LinkedList<Integer>();
	}
	
	/**
    * Returns the number of children this fragment has
    * @return an int
    */
    public int numChildren() {
		return childrenIDs.size();
	}
		
	/**
    * Adds a child's ID to this fragment
    * @param ID - the ID to add
    */
    public void addChild(int ID) {
    	if (!childrenIDs.contains(ID)) {
			childrenIDs.add(ID);
		}
	}
	
	@Override
	public String toString() {
		String str = "Fragment[" + value + "->";
		for(int i = 0; i < childrenIDs.size(); i++) {
			if (i != 0) {
				str += ", ";
			}
			str += childrenIDs.get(i);
		}
		str += "]";
		return str;
	}
     
    /**
    * Counts the number of fragments with children
    * @param list - the list to count through
    * @return an int
    */
    public static int countNumFragments(Fragment list[]) {
		int counter = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i] != null) {
				if ( (list[i].numChildren() != 0) || (list[i].hasOutput) ) {
                    counter++;
                }
			}
		}
		return counter;
	}
    
    /**
    * Figures out the maximum number of children for any one fragment in the list
    * @param list - the list to count rhough
    * @return an int
    */
    public static int maxNumChildren(Fragment list[]) {
    	int maxNumChildren = 0;
    	for(Fragment f : list) {
		    if (f != null) {
		        maxNumChildren = Math.max(maxNumChildren, f.numChildren());
		    }
		}
    	return maxNumChildren;
    }
    

	
	/**
    * Sorts the fragment's children in ascending order
    */
    public static Fragment[] sortFragmentList(Fragment list[]) {
    	for (Fragment f: list) {
    		if (f != null) {
				if (f.childrenIDs.size() > 1) {
					int sz = f.childrenIDs.size();
					for(int i = 0; i < sz; i++) {
						int min = Integer.MAX_VALUE;
						int min_index = i;
						
						for (int j = i; j < sz; j++) {
							int val = list[f.childrenIDs.get(j)].value;
							if (val < min) {
								min = val;
								min_index = j;
							}
						}
						
						if (min_index != i) {
							min = f.childrenIDs.get(i);
							f.childrenIDs.set(i, f.childrenIDs.get(min_index));
							f.childrenIDs.set(min_index, min);
						}
					}
				}
    		}
    	}
    	
    	return list;
	}
}