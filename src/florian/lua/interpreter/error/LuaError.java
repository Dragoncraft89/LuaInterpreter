package florian.lua.interpreter.error;

import florian.lua.interpreter.LuaInterpreter;

public class LuaError extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2797917155279798624L;
	private Error error;

	
	public LuaError(Error error) {
		super(error.getMessage());
		this.error = error;
	}
	
	public void printStacktrace(LuaInterpreter p) {
		error.printStacktrace(p);
	}
}
