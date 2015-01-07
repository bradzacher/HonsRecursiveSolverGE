import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

//import javax.swing.BorderFactory;
import javax.swing.JComponent;

/**
* Displays a child-parent link between two nodes
*/
public class LinkIcon extends JComponent implements BuilderWindowIcon, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	// state vars
	private boolean selected = false;
	private boolean hover = false;
	
	/**
	 * The fragment this link starts at - the PARENT
	 */
	public final FragmentParentIcon start;
	/**
	 * The fragment this link finishes at - the CHILD
	 */
	public final FragmentParentIcon finish;
	
	// drawing stuff
	private final Color ROAD_COLOUR = new Color(170, 0, 255);
	private final Color SELECTED_ROAD_COLOUR = new Color(200, 170, 255);
	private final Color HOVER_ROAD_COLOUR = new Color(115, 0, 200);
	private final int LINE_WIDTH = 4;
	//the drawn polygon
	private Polygon roadPolygon;
	private int xpoints[] = null;
	private int ypoints[] = null;
	private int sX;
	private int sY;
	private int fX;
	private int fY;
	
    /**
    * Default constructor
    * @param rootParent - the BuilderWindowPanel to which this Icon is drawn
    * @param start - the PARENT from which this link originates
    * @param finish - the CHILD to which this link terminates
    */
	public LinkIcon(FragmentParentIcon start, FragmentParentIcon finish) {		
		this.start = start;
		this.finish = finish;
		
		start.addChangeListener(this);
		finish.addChangeListener(this);
		
		this.makeLine();
		
		this.setLayout(null);
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		Environment.panelChanged(true);
		
		//this.setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	/**
	* Figures out how to draw a rectangle with ends perpendicular to the line between the two places
	* uses the formula from http://stackoverflow.com/questions/2219108/knowing-two-points-of-a-rectangle-how-can-i-figure-out-the-other-two/2219153#2219153
	*/
	private void makeLine() {
		sX = start.getX() + FragmentParentIcon.CIRCLE_RADIUS;
		sY = start.getY() + FragmentParentIcon.BUTTON_DIAMETER + FragmentParentIcon.CIRCLE_DIAMETER + FragmentParentIcon.BUTTON_RADIUS;
		fX = finish.getX() + FragmentParentIcon.CIRCLE_RADIUS;
		fY = finish.getY() + FragmentParentIcon.BUTTON_RADIUS;
		
		//special case when the points are on top of each other
		if ((sX == fX) && (sY == fY)) {
			int xtemp[] = {0,0,0,0};
			int ytemp[] = {0,0,0,0};
			
			xpoints = xtemp;
			ypoints = ytemp;
			
		//special case - horizontal line
		} else 	if (sX == fX) {
			int xtemp[] = {sX + LINE_WIDTH, sX - LINE_WIDTH, fX - LINE_WIDTH, fX + LINE_WIDTH};
			int ytemp[] = {sY, sY, fY, fY};
			
			xpoints = xtemp;
			ypoints = ytemp;
			
		//special case - vertical line
		} else if (sY == fY) {
			int xtemp[] = {sX, sX, fX, fX};
			int ytemp[] = {sY + LINE_WIDTH, sY - LINE_WIDTH, fY - LINE_WIDTH, sY + LINE_WIDTH};
			
			xpoints = xtemp;
			ypoints = ytemp;
			
		//every other line
		} else {			
			double m1 = ((double)(fY - sY)) / ((double)(fX - sX));
			double m2 = - 1.0 / m1;
			
			double dx = Math.sqrt( Math.pow(LINE_WIDTH, 2) / (1 + Math.pow(m2, 2)) ) / 2;
			double dy = m2 * dx;
						
			int xtemp[] = {(int)Math.round(sX + dx), (int)Math.round(sX - dx), (int)Math.round(fX - dx), (int)Math.round(fX + dx)};
			int ytemp[] = {(int)Math.round(sY + dy), (int)Math.round(sY - dy), (int)Math.round(fY - dy), (int)Math.round(fY + dy)};
			
			xpoints = xtemp;
			ypoints = ytemp;
		}
		
		int minX = Math.min(sX, fX);
		int minY = Math.min(sY, fY);
		
		this.roadPolygon = new Polygon(xpoints, ypoints, 4);
		this.roadPolygon.translate(-minX + LINE_WIDTH, -minY + LINE_WIDTH);
				
		int width = Math.abs(sX - fX);
		int height = Math.abs(sY - fY);
		// the sums in here are a fudge to stop the box from disappearing when horizontal or vertical
		this.setBounds(minX - LINE_WIDTH, minY - LINE_WIDTH, width + (LINE_WIDTH + LINE_WIDTH), height + (LINE_WIDTH + LINE_WIDTH));
	}
	
	/**
    * Updates the line when an attached node is moved
    */
	public void iconMoved() {
		this.makeLine();
		this.repaint();
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
    public boolean contains(int x, int y) {
		return roadPolygon.contains(x, y);
	}
    
    @Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void deleteMe() {
        // remove the link from the content pane
		Environment.contentPane.remove(this);
        
        // stop listening to the start and finish nodes
        start.removeChangeListener(this);
        finish.removeChangeListener(this);
		
        Environment.panelChanged(true);
	}
	
	@Override
	public void toggleSelect() {
		this.selected = !this.selected;
	}

	@Override
	public boolean intersects(Rectangle2D bounds) {
		return bounds.intersectsLine(sX, sY, fX, fY);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	
		//decide what colour to draw the base circle in
		if (selected) {
			g.setColor(SELECTED_ROAD_COLOUR);
		} else if (hover) {
			g.setColor(HOVER_ROAD_COLOUR);
		} else {
			g.setColor(ROAD_COLOUR);
		}
		
		g.fillPolygon(roadPolygon);
		g.drawPolygon(roadPolygon);
	}
    
    @Override
	public String toString() {
		return "LinkIcon [" + start + ", " + finish + "]";
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (Environment.contentPane.isLinkingMode()) {
			this.dispatchToRoot(e);
		} else {
			this.selected = !this.selected;
			
			this.repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.hover = true;
		this.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.hover = false;
		this.repaint();
	}
    
	@Override
	public void mouseReleased(MouseEvent e) {
		this.dispatchToRoot(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		dispatchToRoot(e);
	}
	
    @Override
	public void mouseDragged(MouseEvent e) {}
    @Override
	public void mousePressed(MouseEvent e) {}
}
