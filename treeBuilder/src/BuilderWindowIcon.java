import java.awt.geom.Rectangle2D;

/**
* Interface defining the common methods for a displayed Icon
*/
public interface BuilderWindowIcon {
    /**
    * Checks if the Icon is selected
    * @return true if selected, false otherwise
    */
	public boolean isSelected();
	
	/**
    * Removes the Icon from everywhere it knows it is referenced
    */
	public void deleteMe();
	
	/**
    * Toggles the selection status of the Icon
    */
	public void toggleSelect();
	
	/**
    * Checks if the Icon intersects the rectangle
    * @param r - the rectangle to check intersection with
    * @return true if they intersect, false otherwise
    */
	public boolean intersects(Rectangle2D r);
}
