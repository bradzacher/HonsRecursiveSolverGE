import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GAPhenotypeDisplayer extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public GAPhenotypeDisplayer(GAStatContainer gsc) {
		super("Best Phenotype");
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setContentPane(new GABestPhenotypePanel(gsc, this));
		
        //Show the window.
        this.pack();
        this.validate();
        this.setVisible(true);
	}
	
	private class GABestPhenotypePanel extends JPanel implements GABestPhenotypeListener {
		private static final long serialVersionUID = 1L;

		private final GAStatContainer gsc;
		private final GAPhenotypeDisplayer parentFrame;

		private final int MAX_LINE_LENGTH = 40;
		private final Dimension MAX_DIMENSION = new Dimension(413, 212);
		
		private final Color TEXT_COLOUR = new Color(0, 0, 0);
		private final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.BOLD, 18);
		
		private int CHAR_WIDTH;
		private int CHAR_HEIGHT;
		private int FIRST_LINE_X;
		private int FIRST_LINE_Y;
		
		
		private String bestPhenotype[] = {""};
		
		public GABestPhenotypePanel(GAStatContainer gsc, GAPhenotypeDisplayer gpd) {
			super();
			this.gsc = gsc;
			this.parentFrame = gpd;
			gsc.addBestPhenotypeListener(this);
			
			this.setPreferredSize(MAX_DIMENSION);
			this.setMinimumSize(MAX_DIMENSION);
			this.setMaximumSize(MAX_DIMENSION);
			
			this.setLayout(null);
		}
		
		public void paintComponent(Graphics g) {
			FontMetrics metrics = g.getFontMetrics(TEXT_FONT);
			
			CHAR_WIDTH = metrics.stringWidth("A");
			CHAR_HEIGHT = metrics.getHeight();
			FIRST_LINE_X = CHAR_WIDTH * 2;
			FIRST_LINE_Y = CHAR_HEIGHT * 2;
			
			super.paintComponent(g);
			
			g.setFont(TEXT_FONT);
			g.setColor(TEXT_COLOUR);
			for (int i = 0; i < bestPhenotype.length; i++) {
				String s = bestPhenotype[i];
				
				g.drawString(s, FIRST_LINE_X, FIRST_LINE_Y + (i * CHAR_HEIGHT));
			}
		}
		
		@Override
		public void bestPhenotypeChanged() {
			String phenotype = gsc.getBestPhenotype().replace("/**/", "");
			this.bestPhenotype = phenotype.split("\n");
			
			int max = -1;
			int lengthSoFar = 0;
			int i = 0;
			
			//if our line is very long, split it up
			while (true) {
				if (i >= bestPhenotype.length) {
					break;
				}
				
				String s = bestPhenotype[i];
				if (s.length() >= MAX_LINE_LENGTH) {
					int index = phenotype.substring(0, lengthSoFar + MAX_LINE_LENGTH).lastIndexOf('(');
					phenotype = phenotype.substring(0, index+1) + "\n            " + phenotype.substring(index+1);
					bestPhenotype = phenotype.split("\n");
					i = 0;
					lengthSoFar = 0;
					continue;
				}
				
				
				max = Math.max(s.length(), max);
				lengthSoFar += s.length();
				i++;
			}
			
			int width = FIRST_LINE_X * 3 + CHAR_WIDTH * max;
			int height = FIRST_LINE_Y * 2 + CHAR_HEIGHT * bestPhenotype.length;
			parentFrame.setBounds(parentFrame.getX(), parentFrame.getY(), width, height);
			this.repaint();
		}
	}
}
