import javax.swing.JTextArea;

/**
 * Convenience class, makes a JTextArea look like a PrintStream so that I can easily interchange the two
 */
public class TextAreaPrintStream {
	private JTextArea text = null;
	
	private String content = "";
	
	/**
	 * Basic constructor
	 * @param text - null if no JTextArea is needed, otherwise the JTextArea to append to
	 */
	public TextAreaPrintStream(JTextArea text) {
		this.text = text;
	}

	/**
	 * Appends some text to the stream
	 * @param s - the string to append
	 */
	public void append(String s) {
		if (text == null) {
			System.out.print(s);
			this.content += s;
		} else {
			this.text.append(s);
		}
	}
	
	/**
	 * Grab the text as an array of lines
	 * @return the lines
	 */
	public String[] getText() {
		if (text == null) {
			return content.split("\n");
		} else {
			try {
				return text.getDocument().getText(0, this.text.getDocument().getLength()).split("\n");
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	/**
	 * Figure out how many characters are in the stream
	 * @return the number of characters
	 */
	public int length() {
		if (text == null) {
			return content.length();
		} else {
			return text.getDocument().getLength();
		}
	}
}
