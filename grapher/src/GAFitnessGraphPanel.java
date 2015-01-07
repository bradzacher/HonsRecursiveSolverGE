import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import javax.swing.JPanel;

public class GAFitnessGraphPanel extends JPanel implements GAAverageFitnessListener, GABestFitnessListener {
		private static final long serialVersionUID = 1L;

		private final int NUM_GENERATIONS = 250;
		
		private final GAStatContainer gsc;
		private final Dimension MAX_DIMENSION = new Dimension(852, 451);
		private int GRAPH_AREA_WIDTH = 800;
		public int getGraphAreaWidth() {
			return GRAPH_AREA_WIDTH;
		}
		private int GRAPH_AREA_HEIGHT = 400;
		public int getGraphAreaHeight() {
			return GRAPH_AREA_HEIGHT;
		}
		public final int AXES_AREA_SIZE = 50;
		private final double FITNESS_TICKS = 10;
		
		private int X_AXIS_TICK_LOCATION = GRAPH_AREA_HEIGHT + 10;
		private final int Y_AXIS_TICK_LOCATION = AXES_AREA_SIZE - 10;
		
		private double X_SCALE = (double)GRAPH_AREA_WIDTH / (double)NUM_GENERATIONS;
		public double getXScale() {
			return X_SCALE;
		}
		private double Y_SCALE = (double)GRAPH_AREA_HEIGHT / FITNESS_TICKS;
		public double getYScale() {
			return Y_SCALE;
		}
		
		private final Color AVERAGE_FITNESS_COLOUR = new Color(0, 0, 255);
		private final Color BEST_FITNESS_COLOUR = new Color(0, 255, 0);
		private final Font AXES_LABEL_FONT = new Font(Font.MONOSPACED, Font.BOLD, 18);
		private final Font AXES_TICK_FONT = new Font(Font.MONOSPACED, Font.BOLD, 13);
		
		private final int ENTIRE_SPACE_Y_TRANSLATE = 1;
		
		private final BasicStroke LINE_STROKE = new BasicStroke(2);
		
		private LinkedList<GAFitnessGraphDataPoint> averageFitnessPoints = new LinkedList<GAFitnessGraphDataPoint>();
		private LinkedList<GAFitnessGraphDataPoint> bestFitnessPoints = new LinkedList<GAFitnessGraphDataPoint>();
		
		public GAFitnessGraphPanel(GAStatContainer gsc) {
			super();
			this.gsc = gsc;
			
			gsc.addAverageFitnessListener(this);
			gsc.addBestFitnessListener(this);

			this.setPreferredSize(MAX_DIMENSION);
			this.setMinimumSize(MAX_DIMENSION);
			this.setMaximumSize(MAX_DIMENSION);
			
			this.setLayout(null);
		}
		
		public void paintComponent(Graphics g) {
			GRAPH_AREA_WIDTH = this.getWidth() - AXES_AREA_SIZE - 2;
			GRAPH_AREA_HEIGHT = this.getHeight() - AXES_AREA_SIZE - 1;
			X_AXIS_TICK_LOCATION = GRAPH_AREA_HEIGHT + 10;
			X_SCALE = (double)GRAPH_AREA_WIDTH / (double)NUM_GENERATIONS;
			Y_SCALE = (double)GRAPH_AREA_HEIGHT / FITNESS_TICKS;

			super.paintComponent(g);
			g.translate(0, ENTIRE_SPACE_Y_TRANSLATE);
			
			Graphics2D g2d = (Graphics2D)g;
			
			// DRAW THE AXES LABELS
			g.setFont(AXES_LABEL_FONT);
			AffineTransform saveAT = g2d.getTransform(); // Save the original transformation
			AffineTransform fitnessLocation = AffineTransform.getTranslateInstance(15, (this.getHeight() / 2));
			fitnessLocation.rotate(-Math.PI/2.0);
			g2d.setTransform(fitnessLocation); // Rotate for the fitness label
			g2d.drawString("FITNESS", 0, 0);
			g2d.setTransform(saveAT); // Undo rotate
			g2d.drawString("GENERATION", (this.getWidth() / 2) - 5*10, (this.getHeight() - 5)); //5 units of width per character
			
			g.setFont(AXES_TICK_FONT);
			g2d.drawString("0", Y_AXIS_TICK_LOCATION, X_AXIS_TICK_LOCATION);
			// Draw the Y-Axis tick labels
			for (int i = 1; i < 6; i++) {
				int labelIncrement = 50;
				int increment = AXES_AREA_SIZE + (int) (i * X_SCALE * labelIncrement);
				String str = labelIncrement * i + "";
				g2d.drawString(str, increment + 2, X_AXIS_TICK_LOCATION + 5);
				g2d.drawLine(increment, X_AXIS_TICK_LOCATION, increment, X_AXIS_TICK_LOCATION-10);
			}
			
			// Draw the X-Axis tick labels
			for (int i = 1; i < 11; i++) {
				int increment = GRAPH_AREA_HEIGHT - (int)(i * Y_SCALE);
				String str = ((double)i / 10.0) + "";
				g2d.drawString(str, Y_AXIS_TICK_LOCATION-18, increment + 13);
				g2d.drawLine(Y_AXIS_TICK_LOCATION, increment, AXES_AREA_SIZE, increment);
			}
			
			// COLOUR THE GRAPH AREA
			g.setColor(Color.WHITE);
			g.fillRect(AXES_AREA_SIZE, 0, GRAPH_AREA_WIDTH, GRAPH_AREA_HEIGHT);
			g.setColor(Color.BLACK);
			g.drawRect(AXES_AREA_SIZE, 0, GRAPH_AREA_WIDTH, GRAPH_AREA_HEIGHT);
			
			// DRAW THE LINES
			g2d.setStroke(LINE_STROKE);
			
			g2d.setColor(AVERAGE_FITNESS_COLOUR);
			int len = averageFitnessPoints.size();
			for (int i = 1; i < len; i++) {
				GAFitnessGraphDataPoint p1 = averageFitnessPoints.get(i-1);
				GAFitnessGraphDataPoint p2 = averageFitnessPoints.get(i);
				g2d.drawLine(p1.getAdjustedX(), p1.getAdjustedY(), p2.getAdjustedX(), p2.getAdjustedY());
			}

			g2d.setColor(BEST_FITNESS_COLOUR);
			len = bestFitnessPoints.size();
			for (int i = 1; i < len; i++) {
				GAFitnessGraphDataPoint p1 = bestFitnessPoints.get(i-1);
				GAFitnessGraphDataPoint p2 = bestFitnessPoints.get(i);
				g2d.drawLine(p1.getAdjustedX(), p1.getAdjustedY(), p2.getAdjustedX(), p2.getAdjustedY());
			}	
		}
		
		private boolean getNewValues(GAStatListener.Command theCommand){//, Point2D p) {
			GAFitnessGraphDataPoint startPoint;
			GAFitnessGraphDataPoint endPoint;
			Point2D e;
			Point2D points[] = null;
			LinkedList<GAFitnessGraphDataPoint> pointList = null;
			Color theColour = null;
			int length;
			// Figure out what fitness set we changed
			switch (theCommand) {
				// Added a/some new average fitness point(s)
				case AVERAGE_FITNESS:
						points = gsc.getAverageFitnessPoints();
						length = points.length;
						pointList = averageFitnessPoints;
						theColour = AVERAGE_FITNESS_COLOUR;
					break;
				
				case BEST_FITNESS:
						points = gsc.getBestFitnessPoints();
						length = points.length;
						pointList = bestFitnessPoints;
						theColour = BEST_FITNESS_COLOUR;
					break;
					
				default:
						return true;
			}
			
			// if we have the same number of points as the GAStatContainer, do nothing
			if (pointList.size() < length) {
				// Add each point we don't have
				for (int i = pointList.size(); i < length; i++) {
					if (pointList.isEmpty()) {
						startPoint = new GAFitnessGraphDataPoint(theColour, AXES_AREA_SIZE, (double)GRAPH_AREA_HEIGHT);
						pointList.addFirst(startPoint);
					} else {
						startPoint = pointList.peekLast();
					}
					
					e = points[i];

					endPoint = new GAFitnessGraphDataPoint(theColour, (int)e.getX(), e.getY());
					this.add(endPoint);
					endPoint.reposition();
					pointList.addLast(endPoint);
				}
			}
			
			return true;
		}


		@Override
		public void averageFitnessChanged(){//Point2D p) {
			getNewValues(GAStatListener.Command.AVERAGE_FITNESS);//, p);
			repaint();
		}
		
		@Override
		public void bestFitnessChanged(){//Point2D p) {
			getNewValues(GAStatListener.Command.BEST_FITNESS);//, p);
			repaint();
		}
	}
