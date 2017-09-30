package florian.lua;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Output-wrapper for standarized output of errors, messages, etc
 * @author Florian
 *
 */
public class Output {
	private OutputStream out;
	
	public Output() {
		out = System.out;
	}
	
	public Output(OutputStream out) {
		this.out = out;
	}
	
	public void printMessage(String message) {
		write("[Message] " + message + "\r\n");
	}
	
	public void printError(String error) {
		write("[Lua Error] " + error + "\r\n");
	}

	public void print(String string) {
		write(string + "\r\n");
	}
	
	private void write(String message) {
		try {
			out.write(message.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
