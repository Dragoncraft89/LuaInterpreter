package florian.lua.interpreter.functions;

import florian.lua.interpreter.statement.FunctionStatement;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.VarType;

/**
 * This class represents a lua call in the stack
 * @author Florian
 *
 */
public class Call {
	private String functionName;
	private FunctionStatement function;
	private VarType[] params;
	
	public Call(String functionName, FunctionStatement function, VarType[] params) {
		this.functionName = functionName;
		this.function = function;
		this.params = params;
	}
	
	public String getFunctionName() {
		return functionName;
	}

	public Statement getFunction() {
		return function;
	}
	
	public VarType[] getParams() {
		return params;
	}
}
