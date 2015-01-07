import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

public class GAFitnessGraphDataPoint extends JComponent implements MouseListener {
	private static final long serialVersionUID = 1L;
	
	private final int CIRCLE_RADIUS = 3;
	private final int CIRCLE_DIAMETER = 2 * CIRCLE_RADIUS;
	private final int BOX_WIDTH = 65;
	private final int BOX_HEIGHT = 20;
	private final int BOX_OFFSET_X = 8;
	private final int BOX_OFFSET_Y = 3;
	
	private Color CURRENT_COLOUR;
	private final Color NORMAL_COLOUR;
	private final Color HOVER_COLOUR;
	private final Color BOX_OUTLINE = Color.BLACK;
	private final Color BOX_FILL = Color.YELLOW;
	
	private final Color TEXT_COLOUR = Color.BLACK;
	private final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.BOLD, 10);
	
	public double X;
	public double Y;
	
	private boolean hover = false;
	
	private final int GENERATION;
	private final double FITNESS;
	
	private Rectangle ORIGINAL_BOUNDS;
	private Rectangle BIGGER_BOUNDS;
	
	
	public GAFitnessGraphDataPoint(Color c, int generation, double fitness) {
		super();
		
		this.GENERATION = generation;
		this.FITNESS = fitness;
		this.X = generation;
		this.Y = fitness;
		
		this.NORMAL_COLOUR = c;
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		
		r = Math.min(255, r + 200);
		g = Math.min(255, g + 200);
		b = Math.min(255, b + 200);
		
		this.HOVER_COLOUR = new Color(r,g,b);
		CURRENT_COLOUR = NORMAL_COLOUR;
		
		ORIGINAL_BOUNDS = new Rectangle((int)X - CIRCLE_RADIUS, (int)Y - CIRCLE_RADIUS, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
		BIGGER_BOUNDS = new Rectangle((int)X - CIRCLE_RADIUS, (int)Y - CIRCLE_RADIUS, CIRCLE_DIAMETER + BOX_WIDTH + BOX_OFFSET_X + 1, CIRCLE_DIAMETER + BOX_HEIGHT + BOX_OFFSET_Y);
		
		this.setBounds(ORIGINAL_BOUNDS);
		this.addMouseListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		reposition();
		
		g.setColor(CURRENT_COLOUR);
		g.fillOval(0, 0, CIRCLE_DIAMETER, CIRCLE_DIAMETER);
		
		if (hover) {
			g.setColor(BOX_OUTLINE);
			g.drawRect(CIRCLE_DIAMETER + BOX_OFFSET_X, BOX_OFFSET_Y, BOX_WIDTH, BOX_HEIGHT);
			g.setColor(BOX_FILL);
			g.fillRect(CIRCLE_DIAMETER + BOX_OFFSET_X, BOX_OFFSET_Y, BOX_WIDTH, BOX_HEIGHT);
			
			g.setColor(TEXT_COLOUR);
			g.setFont(TEXT_FONT);
			g.drawString("GEN:" + GENERATION, CIRCLE_DIAMETER+BOX_OFFSET_X + 2, BOX_OFFSET_Y + 8);
			g.drawString("FIT:" + String.format("%1.4f", FITNESS), CIRCLE_DIAMETER+BOX_OFFSET_X + 2, BOX_OFFSET_Y + 16);
		}
	}
	
	public void reposition() {
		GAFitnessGraphPanel parent = (GAFitnessGraphPanel)this.getParent();
		double X_SCALE = parent.getXScale();
		X = parent.AXES_AREA_SIZE + GENERATION * X_SCALE;
		Y = parent.getGraphAreaHeight() - FITNESS * parent.getGraphAreaHeight();
		
		// If we've moved enough from scaling, reposition
		if ((Math.abs(X - this.getX()) > 0.001) || (Math.abs(Y - this.getY()) > 0.001)) {
			ORIGINAL_BOUNDS.x = (int) X - CIRCLE_RADIUS;
			ORIGINAL_BOUNDS.y = (int) Y - CIRCLE_RADIUS;
			
			BIGGER_BOUNDS.x = (int) X - CIRCLE_RADIUS;
			BIGGER_BOUNDS.y = (int) Y - CIRCLE_RADIUS;
			
			if (hover) {
				this.setBounds(BIGGER_BOUNDS);
			} else {
				this.setBounds(ORIGINAL_BOUNDS);
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Change Colour
		CURRENT_COLOUR = HOVER_COLOUR;
		// Toggle Hover Status
		hover = true;
		// Make the bounding box big enough to fit the info box
		this.setBounds(BIGGER_BOUNDS);
		// Cause a repaint
		this.getParent().repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Change Colour
		CURRENT_COLOUR = NORMAL_COLOUR;
		// Toggle Hover Status
		hover = false;
		// Make the bounding box big enough to fit the info box
		this.setBounds(ORIGINAL_BOUNDS);
		// Cause a repaint
		this.getParent().repaint();
	}
	
	@Override
	public boolean contains(int x, int y) {
		return (Math.pow(CIRCLE_RADIUS - x, 2) + Math.pow(CIRCLE_RADIUS - y, 2)) < Math.pow(CIRCLE_RADIUS, 2); 
	}

	public int getAdjustedX() {
		return (int) (X);// + CIRCLE_RADIUS);
	}

	public int getAdjustedY() {
		return (int) (Y);// + CIRCLE_RADIUS);
	}
	
	public String toString() {
		return "GAFitnessGraphDataPoint[FITNESS="+FITNESS+",GENERATION="+GENERATION+",X="+X+",Y="+Y+"]";
	}
}
