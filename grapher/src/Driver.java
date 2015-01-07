public class Driver {
    private static enum CmdFlag {
        FITNESS_GRAPHER, FG, PHENOTYPE_DISPLAYER, PD, NOVALUE;
        
        public static CmdFlag toFlag(String s) {
            try {
                if (s.charAt(0) != '-') {
                    throw new Exception();
                }
                return valueOf(s.replace("-", ""));
            } catch (Exception e) {
                return NOVALUE;
            }
        }
        
        public int toIndex() {
            switch (this) {
                case FG:
                case FITNESS_GRAPHER:
                    return 0;
                
                case PD:
                case PHENOTYPE_DISPLAYER:
                    return 1;
                
                default:
                    return -1;
            }
        }
    }
    
    private static boolean options[] = new boolean[2];
    
    /**
    * USAGE: no args for all windows
    *        -FITNESS_GRAPHER (or -FG) for the Fitness Graph
    *        -PHENOTYPE_DISPLAYER (or -PD) for the Phenotype Displayer
    */
	public static void main(String[] args) throws Exception {
		GAStatContainer gsc = new GAStatContainer();
		
    	GAStatListener gsl = new GAStatListener(gsc);
    	
        parseArgs(args);
        
        if (options[0]) {
            new GAFitnessGrapher(gsc);
        }
        if (options[1]) {
            new GAPhenotypeDisplayer(gsc);
        }
    	
        gsl.startListener(); // LISTEN!!
    }
    
    private static void parseArgs(String[] args) {
        if (args.length == 0) {
            for (int i = 0; i < options.length; i++) {
                options[i] = true;
            }
        }
        
        for (String s : args) {
            int i = CmdFlag.toFlag(s).toIndex();
            if (i != -1) {
                options[i] = true;
            }
        }
    }
}
