import java.io.IOException;
import java.io.InputStream;
 
/**
 * Provides a convenient way for java to run the C++ Genetic Algorithm and play with the output
 */
public class GEInterface {
	/**
	 * Convenience class to handle the output from the process in a separate thread to prevent blocking
	 */
	private class StreamWrapper extends Thread {
	    private InputStream is = null;
	    private TextAreaPrintStream output = null;
	 
	    /**
	     * Basic constructor
	     * @param is - the inputstream to read from
	     * @param output - the container to write to
	     */
	    public StreamWrapper(InputStream is, TextAreaPrintStream output) {
	        this.is = is;
	        this.output = output;
	    }
	 
	    @Override
	    public void run() {
	        try {
	        	int c = 0;
	        	while ( (c = is.read()) != -1) {
	                this.output.append(Character.toString((char)c));
	            }
	        } catch (IOException ioe) {
	        	ioe.printStackTrace(); 
	        }
		}
	}
	
	private Process proc = null;
	private TextAreaPrintStream text = null;
	
	private GEInterface() {}
	
	/**
	 * Builds a GEInterface running GEGCC
	 * @param text - the stream to handle the output
	 * @param args - the args to pass to the GE
	 * @return the built GEInterface
	 */
	public static GEInterface GEInterfaceFactory(TextAreaPrintStream text, String args[]) {
		return GEInterfaceFactory(text, args, "./GEGCC");
	}
	
	/**
	* Creates a GEInterface running the specified process
	* @param text - the stream to handle the output
	* @param args - the args to pass to the process
	* @param process - the actual cmd-line command to run
	* @return the built GEInterface
	*/
	public static GEInterface GEInterfaceFactory(TextAreaPrintStream text, String args[], String process) {
		GEInterface rte = null;
		try {
			Runtime rt = Runtime.getRuntime();
			rte = new GEInterface();
			StreamWrapper error = null;
			StreamWrapper output = null;
			
			String argStr = " -nv";
			for(String s : args) {
				argStr += " " + s;
			}
			//System.out.println(process + argStr);
			
			Process proc = rt.exec(process + argStr);
			error = rte.new StreamWrapper(proc.getErrorStream(), text);
			output = rte.new StreamWrapper(proc.getInputStream(), text);
			
			output.start();
			error.start();
			
			rte.proc = proc;
			rte.text = text;
		} catch (IOException e) {
			e.printStackTrace();
        }
		
		return rte;
	}
	
	/**
	 * Append a string to the output container
	 * @param s - the string to append
	 */
	public void append(String s) {
		this.text.append(s);
	}
	
	/**
	* Waits for the associated process to exit and returns its exit code
	* @return the exit code
	*/
	public int getExitValue() {
		try {
			return proc.waitFor();
		} catch (InterruptedException e) {
			return Integer.MIN_VALUE;
		}
	}
}
