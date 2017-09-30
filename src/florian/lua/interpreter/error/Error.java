package florian.lua.interpreter.error;

import florian.lua.Output;
import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.functions.Call;

public class Error {
	private String message;
	private Call[] stack;

	public Error(String message, Call[] stack) {
		this.message = message;
		this.stack = stack;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void printStacktrace(LuaInterpreter p) {
		Output o = p.getOutput();
		if(stack.length != 0) {
			Call head = stack[0];
			String s = "Error in function \'" + head.getFunctionName() + "\': " + message;
			s += "\n\nStacktrace:";
			for(int i = 0; i < stack.length; i++) {
				s += "\n" + stack[i].getFunction().getLine() + ": in function \'" + stack[i].getFunctionName() + "\'";
			}
			o.printError(s);
		}
	}
}
