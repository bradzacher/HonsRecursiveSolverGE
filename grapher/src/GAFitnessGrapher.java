import javax.swing.JFrame;

public class GAFitnessGrapher extends JFrame {
	private static final long serialVersionUID = 1L;
		
	public GAFitnessGrapher(GAStatContainer gsc) {
		super("Fitness Graph");
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setContentPane(new GAFitnessGraphPanel(gsc));
		
        //Show the window.
        this.pack();
        this.validate();
        this.setVisible(true);
	}
}
