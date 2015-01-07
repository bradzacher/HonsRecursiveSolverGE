import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

//import javax.swing.BorderFactory;
import javax.swing.JComponent;

/**
* Displays a node in the call tree
*/
public class FragmentParentIcon extends JComponent implements MouseListener, MouseMotionListener, BuilderWindowIcon {
	private static final long serialVersionUID = 1L;
	
	public static int ID_COUNTER = 0;
	
	/**
	 * The input value (function parameter) this node represents
	 */
	public final int value;
	/**
	 * The icon's drawn label (combination of value and outputValue)
	 */
	private final String label[];
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
	public final int ID;
	
	/**
	 * The icon's actual point
	 */
	private Point globalPosition = new Point(0,0);
	
	// stuff for dragging and moving the icon
	private Point lastPos = new Point(0,0);
	private boolean selected;
	private boolean hover;
	private boolean dragging;
	
	// stuff for drawing the icon
	public final static int CIRCLE_RADIUS = 30;
	public final static int CIRCLE_DIAMETER = CIRCLE_RADIUS * 2;
	public final static int BUTTON_RADIUS = 4;
	public final static int BUTTON_DIAMETER = 2 * BUTTON_RADIUS;
	private final Color CIRCLE_COLOUR = new Color(255, 0, 0);
	private final Color BUTTON_COLOUR = new Color(0, 0, 0);
	private final Color HOVER_CIRCLE_COLOUR = new Color(150, 0, 0);
	private final Color SELECTED_CIRCLE_COLOUR = new Color(255, 180, 180);
	private final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.BOLD, 18);
	private final Color TEXT_COLOUR = new Color(0, 0, 0);
	private final Dimension DIMENSION = new Dimension(CIRCLE_DIAMETER, CIRCLE_DIAMETER + (2 * BUTTON_DIAMETER));
	
	// the links that are attached to the icon
	private LinkedList<LinkIcon> changeListeners = new LinkedList<LinkIcon>();
	
	/**
    * Constructs an Icon with parameter value and outputValue
    * @param value - the parameter value to associate with this node
    * @param outputValue - the outputValue to associate with this node
    * @param rootParent - the BuilderWindowPanel which houses this Icon
    */
	public FragmentParentIcon(int value, int outputValue) {
		this.value = value;
		this.outputValue = outputValue;
		this.hasOutput = true;
		this.label = new String[2];
		this.label[0] = Integer.toString(value);
		this.label[1] = "(" + outputValue + ")";
		
		this.ID = FragmentParentIcon.ID_COUNTER++;
		
		this.construct();
	}
	
	/**
    * Constructs an Icon with parameter value and no outputValue
    * @param value - the parameter value to associate with this node
    * @param rootParent - the BuilderWindowPanel which houses this Icon
    */
	public FragmentParentIcon(int value) {
		this.value = value;
		this.outputValue = Integer.MIN_VALUE;
		this.hasOutput = false;
		this.label = new String[1];
		this.label[0] = Integer.toString(value);
		
		this.ID = FragmentParentIcon.ID_COUNTER++;
		
		this.construct();
	}
	
    /**
    * Finishes off construction of an instance
    */
	private void construct() {
		this.setLayout(null);
		
		//make sure that it's always the exact size we want
		this.setMinimumSize(DIMENSION);
		this.setMaximumSize(DIMENSION);
		this.setPreferredSize(DIMENSION);
		
		//register listener methods
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		this.setBounds(0, 0, CIRCLE_DIAMETER, CIRCLE_DIAMETER + (2 * BUTTON_DIAMETER));
		
		//this.setBorder(BorderFactory.createLineBorder(Color.black));
		
		this.add(new ParentButton());
		this.add(new ChildButton());
		
		Environment.panelChanged(true);
	}

	/**
    * Allows a LinkIcon to listen in on this Icon's changes
    */
	public void addChangeListener(LinkIcon l) {
		changeListeners.add(l);
	}
	
    /**
    * Stops a LinkIcon from listening in on this Icon's changes
    */
	public void removeChangeListener(LinkIcon l) {
		changeListeners.remove(l);
	}
	
	/**
	 * Moves this icon by an amount
	 * @param dx - the amount to move along the x-axis
	 * @param dy - the amount to move along the y-axis
	 */
	public void moveBy(int dx, int dy) {
		int x = this.getX() + dx;
		int y = this.getY() + dy;
		
		this.setLocation(x, y);
		this.globalPosition.setLocation(x, y);
		
		// tell all the connected link icons that this icon has moved
		for (LinkIcon l : changeListeners) {
			l.iconMoved();
		}
	}
	
	/**
    * Makes sure that this Icon is positioned at the coordinates it thinks it is
    */
	public void reposition() {
		this.setLocation(globalPosition.x, globalPosition.y);
		
		// tell all the connected link icons that this icon has moved
		for (LinkIcon l : changeListeners) {
			l.iconMoved();
		}
	}
    
	/**
    * Sends an event to the contentPane for processing
    * @param e - the MouseEvent to send
    */
    private void dispatchToRoot(MouseEvent e) {
		// we modify the event so that the parent receives global coordinates rather than local ones
		e.translatePoint(this.getX(), this.getY());
		Environment.contentPane.dispatchEvent(e);
	}
    
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//decide what colour to draw the base circle in
		if (selected) {
			g.setColor(SELECTED_CIRCLE_COLOUR);
		} else if (hover) {
			g.setColor(HOVER_CIRCLE_COLOUR);
		} else {
			g.setColor(CIRCLE_COLOUR);
		}
		
		//draw the base circle
		g.fillOval(0, BUTTON_DIAMETER, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
		
		//write the text
		g.setColor(TEXT_COLOUR);
		g.setFont(TEXT_FONT);
		if (label.length == 1) {
			g.drawString(label[0], CIRCLE_RADIUS - (5 * label[0].length()), CIRCLE_RADIUS + 8 + BUTTON_DIAMETER);
		} else {
			g.drawString(label[0], CIRCLE_RADIUS - (5 * label[0].length()), CIRCLE_RADIUS - 5 + BUTTON_DIAMETER);
			g.drawString(label[1], CIRCLE_RADIUS - (5 * label[1].length()), CIRCLE_RADIUS + 15 + BUTTON_DIAMETER);
		}
	}
    
    private boolean click = false;
	@Override
	public void mouseClicked(MouseEvent e) {
		if (Environment.contentPane.isLinkingMode()) {
			this.dispatchToRoot(e);
		} else {
			if (this.click) {
				this.selected = false;
				this.click = false;
			} else {
				this.selected = true;
				this.click = true;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		hover = false;
		this.repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && !Environment.contentPane.isLinkingMode()) {
			this.lastPos.setLocation(e.getXOnScreen(), e.getYOnScreen());
			this.dragging = true;
			this.selected = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!Environment.contentPane.isLinkingMode()) {
			this.dragging = false;
			this.selected = false;
			this.repaint();
		}
		this.dispatchToRoot(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (this.dragging) {
			int dx = e.getXOnScreen() - lastPos.x;
			int dy = e.getYOnScreen() - lastPos.y;
			
			this.lastPos.setLocation(e.getXOnScreen(), e.getYOnScreen());

			// move all selected nodes
			Environment.contentPane.moveSelectedBy(dx, dy);
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY() - BUTTON_DIAMETER;
		
		// only make the circle change to hover colour if the mouse is actually over our circle
		if ((Math.pow(CIRCLE_RADIUS - x, 2) + Math.pow(CIRCLE_RADIUS - y, 2)) < Math.pow(CIRCLE_RADIUS, 2)) {
			if (!this.hover) {
				this.hover = true;
				this.repaint();
			}
		} else {
			if (this.hover) {
				this.hover = false;
				this.repaint();
			}
		}
		
		this.dispatchToRoot(e);
	}
	
	@Override
	public boolean isSelected() {
		return this.selected;
	}
	
	@Override
	public void deleteMe() {
		LinkIcon list[] = changeListeners.toArray(new LinkIcon[0]);
		for (LinkIcon l : list) {
			l.deleteMe();
		}

		Environment.panelChanged(true);
	}
	
	@Override
	public void toggleSelect() {
		this.selected = !this.selected;
		this.click = !this.click;
	}
	
	@Override
	public boolean intersects(Rectangle2D bounds) {
		return bounds.intersects(this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public String toString() {
		return "FragmentParentIcon[" + value + "]";
	}
		
	/**
    * Special JComponent which acts as a button to start linking with this node as the parent
	*/
	private class ParentButton extends JComponent implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = 1L;
		private boolean dragging;
		
		/**
        * Default Constructor
        * @param rootParent - the panel who is the direct parent of the FragmentParentIcon
        */
		public ParentButton() {
			this.setBounds(CIRCLE_RADIUS - BUTTON_RADIUS, 0, BUTTON_DIAMETER, BUTTON_DIAMETER);
			//this.setBorder(BorderFactory.createLineBorder(Color.black));
			
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
        
        /**
        * Sends an event to the contentPane for processing
        * @param e - the MouseEvent to send
        */
		public void dispatchToRoot(MouseEvent e) {
			e.translatePoint(this.getX(), this.getY());
			e.translatePoint(this.getParent().getX(), this.getParent().getY());
			Environment.contentPane.dispatchEvent(e);
		}
		
		/**
        * Tells the contentPane to start linking mode with this node as the parent of the link
        */
		public void startLink() {
			Environment.contentPane.startLink((FragmentParentIcon)this.getParent(), true);
		}
		
        @Override
		public void paintComponent(Graphics g) {
			g.setColor(BUTTON_COLOUR);
			g.fillOval(0, 0, BUTTON_DIAMETER, BUTTON_DIAMETER);
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			hover = false;
			this.getParent().repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			hover = false;
			this.getParent().repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				this.dragging = true;

				this.startLink();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			this.dragging = false;
			this.repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragging) {
				this.startLink();

				this.dispatchToRoot(e);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			dispatchToRoot(e);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {}
	}
	
    /**
    * Special JComponent which acts as a button to start linking with this node as the child
	*/
	private class ChildButton extends JComponent implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = 1L;
		private boolean dragging;
		
		/**
        * Default Constructor
        * @param rootParent - the panel who is the direct parent of the FragmentParentIcon
        */
		public ChildButton() {
			this.setBounds(CIRCLE_RADIUS - BUTTON_RADIUS, CIRCLE_DIAMETER + BUTTON_DIAMETER, BUTTON_DIAMETER, BUTTON_DIAMETER);
			//this.setBorder(BorderFactory.createLineBorder(Color.black));
			
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
        
		/**
        * Sends an event to the contentPane for processing
        * @param e - the MouseEvent to send
        */
		public void dispatchToRoot(MouseEvent e) {
			e.translatePoint(this.getX(), this.getY());
			e.translatePoint(this.getParent().getX(), this.getParent().getY());
			Environment.contentPane.dispatchEvent(e);
		}
		
        /**
        * Tells the contentPane to start linking mode with this node as the child of the link
        */
		public void startLink() {
			Environment.contentPane.startLink((FragmentParentIcon)this.getParent(), false);
		}
		
        @Override
		public void paintComponent(Graphics g) {
			g.setColor(BUTTON_COLOUR);
			g.fillOval(0, 0, BUTTON_DIAMETER, BUTTON_DIAMETER);
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			hover = false;
			this.getParent().repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			hover = false;
			this.getParent().repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				this.dragging = true;

				this.startLink();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			this.dragging = false;
			this.repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragging) {
				this.startLink();

				this.dispatchToRoot(e);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			dispatchToRoot(e);
		}
        
        @Override
		public void mouseClicked(MouseEvent e) {}
	}
}
