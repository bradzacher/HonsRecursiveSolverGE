import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serializable;

import javax.swing.JPanel;

/**
* Custom JPanel used as a canvas to draw the nodes and links
*/
public class BuilderWindowPanel extends JPanel implements MouseListener, MouseMotionListener, Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * A place to save the current cursor when we change it
	 */
	private Cursor current;
	
	// stuff for drawing the drag box
	private boolean dragging = false;
	private Point mousePos = new Point(0,0);
	private Point selectionAnchor = new Point(0,0);
	private Polygon selectionBox = null;
	private final Color SELECTION_OUTLINE = new Color(0, 100, 255);
	private final Color SELECTION_FILL = new Color(0, 100, 255, 100);

	// stuff for creating node links
	private Point lineStart = null;
	private FragmentParentIcon lineStartNode = null;
	private boolean lineStartIsParent = false;
	
    /**
    * Default Constructor
    */
 	public BuilderWindowPanel() {
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
	}
	
	/**
    * Checks if the panel is waiting for a second node to be selected
    * @return true if it is, false otherwise
    */
 	public boolean isLinkingMode() {
		return (lineStart != null);
	}
	
	/**
    * Puts the panel into linking mode
    * @param parent - the node that the link was initiated on
    * @param isParent - true if the start node is destined to be the parent of the link, false otherwise
    */
 	public void startLink(FragmentParentIcon parent, boolean isParent) {
		if (this.lineStart == null) {
			//setup the crosshair..
			current = this.getCursor(); //save old cursor
			Cursor crosshair = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
			this.setCursor(crosshair); //change the cursor to a crosshair
		}
		
		if (isParent) {
			lineStart = new Point(parent.getX() + FragmentParentIcon.CIRCLE_RADIUS, parent.getY() + FragmentParentIcon.BUTTON_RADIUS);
			lineStartIsParent = true;
		} else {
			lineStart = new Point(parent.getX() + FragmentParentIcon.CIRCLE_RADIUS, parent.getY() + FragmentParentIcon.BUTTON_DIAMETER + FragmentParentIcon.BUTTON_RADIUS + FragmentParentIcon.CIRCLE_DIAMETER);
			lineStartIsParent = false;
		}
		lineStartNode = parent;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (lineStart != null) {
			g.drawLine(lineStart.x, lineStart.y, mousePos.x, mousePos.y);
		}
		
		if (dragging) {
			int xPoints[] = {selectionAnchor.x, selectionAnchor.x, mousePos.x, mousePos.x};
			int yPoints[] = {selectionAnchor.y, mousePos.y, mousePos.y, selectionAnchor.y};
			
			g.setColor(SELECTION_OUTLINE);
			g.drawPolygon(xPoints, yPoints, 4);
			
			g.setColor(SELECTION_FILL);
			g.fillPolygon(xPoints, yPoints, 4);
			
			//g.drawOval(anchorX, anchorY, 5, 5);
			//g.drawOval(movingX, movingY, 5, 5);
		}
	}

	/**
    * This is a bit of a kludge that makes sure that everything is positioned correctly (i.e. when deserializing)
	*/
	public void reposition() {
		int counter = -1;
		for (Component c : this.getComponents()) {
			if (c instanceof FragmentParentIcon) {
				((FragmentParentIcon)c).reposition();
				// because we are reloading, the static field is not set correctly
				// so we have to figure out what ID we were up to
				counter = Math.max(((FragmentParentIcon)c).ID, counter);
			}
		}
		FragmentParentIcon.ID_COUNTER = counter + 1;
	}
	
	/**
    * Moves all selected nodes by the specified amount
    * @param dx - the amount to move along the x-axis
    * @param dy - the amount to move along the y-axis
    */
 	public void moveSelectedBy(int dx, int dy) {
		for (Component c : this.getComponents()) {
			if (c instanceof FragmentParentIcon) {
				FragmentParentIcon f = (FragmentParentIcon)c;
				if (f.isSelected()) {
					f.moveBy(dx, dy);
				}
			}
		}
		
		Environment.panelChanged(true);
	}
    
    @Override
	public void mouseDragged(MouseEvent e) {
		mousePos.setLocation(e.getX(), e.getY());
		
		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mousePos.setLocation(e.getX(), e.getY());
		
		if (lineStart != null) {
			this.repaint();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (lineStart != null) {
			// if we click on an empty space, finish the joining mode
			if ((e.getSource() instanceof FragmentParentIcon)) {
				if (e.getSource() != lineStartNode) {
					if (lineStartIsParent) {
						this.add(new LinkIcon((FragmentParentIcon)e.getSource(), lineStartNode));
					} else {
						this.add(new LinkIcon(lineStartNode, (FragmentParentIcon)e.getSource()));
					}
				}
			}
			
			lineStart = null;
			
			this.setCursor(current);
			
			this.repaint();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dragging = true;
		
		selectionAnchor.setLocation(e.getX(), e.getY());

		mousePos.setLocation(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (dragging) {
			dragging = false;
	
			int xPoints[] = {selectionAnchor.x, selectionAnchor.x, mousePos.x, mousePos.x};
			int yPoints[] = {selectionAnchor.y, mousePos.y, mousePos.y, selectionAnchor.y};
			selectionBox = new Polygon(xPoints, yPoints, 4);
			
			for (Component c : this.getComponents()) {
				if (c instanceof BuilderWindowIcon) {
					BuilderWindowIcon b = (BuilderWindowIcon)c;
					if (b.intersects(selectionBox.getBounds2D())) {
						b.toggleSelect();
					}
				}
			}
		
			this.repaint();
		}
	}

    @Override
	public void mouseEntered(MouseEvent e) {}
    @Override
	public void mouseExited(MouseEvent e) {}
}