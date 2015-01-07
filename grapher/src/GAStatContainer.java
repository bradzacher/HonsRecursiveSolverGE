import java.awt.geom.Point2D;
import java.util.LinkedList;


public class GAStatContainer {
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private LinkedList<GAAverageFitnessListener> averageFitnessListeners = new LinkedList<GAAverageFitnessListener>();
	
	private LinkedList<Point2D> averageFitnessPoints = new LinkedList<Point2D>();
	
	public Point2D[] getAverageFitnessPoints() {
		return averageFitnessPoints.toArray(new Point2D.Double[0]);
	}
	
	public void addAverageFitnessListener(GAAverageFitnessListener gafl) {
		averageFitnessListeners.add(gafl);
	}
	
	public void addAverageFitnessPoint(double x, double y) {
		averageFitnessPoints.addLast(new Point2D.Double(x, y));
		for (GAAverageFitnessListener gafl : averageFitnessListeners) {
			gafl.averageFitnessChanged();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private LinkedList<GABestFitnessListener> bestFitnessListeners = new LinkedList<GABestFitnessListener>();
	
	private LinkedList<Point2D> bestFitnessPoints = new LinkedList<Point2D>();
	
	public Point2D[] getBestFitnessPoints() {
		return bestFitnessPoints.toArray(new Point2D.Double[0]);
	}
	
	public void addBestFitnessListener(GABestFitnessListener gbfl) {
		bestFitnessListeners.add(gbfl);
	}
	
	public void addBestFitnessPoint(double x, double y) {
		bestFitnessPoints.addLast(new Point2D.Double(x, y));
		for (GABestFitnessListener gbfl : bestFitnessListeners) {
			gbfl.bestFitnessChanged();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private LinkedList<GABestPhenotypeListener> bestPhenotypeListeners = new LinkedList<GABestPhenotypeListener>();
	
	private String bestPhenotype = "";
	
	public String getBestPhenotype() {
		return new String(bestPhenotype);
	}
	
	public void addBestPhenotypeListener(GABestPhenotypeListener gbpl) {
		bestPhenotypeListeners.add(gbpl);
	}
	
	public void setBestPhenotype(String s) {
		bestPhenotype = new String(s);
		for (GABestPhenotypeListener gbpl : bestPhenotypeListeners) {
			gbpl.bestPhenotypeChanged();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
