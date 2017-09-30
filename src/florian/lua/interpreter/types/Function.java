package florian.lua.interpreter.types;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.functions.Call;
import florian.lua.interpreter.statement.FunctionStatement;

public class Function extends VarType {

	private String name;
	private FunctionStatement function;
	
	public Function(String name, FunctionStatement function) {
		this.name = name;
		this.function = function;
	}
	
	@Override
	public VarType operator(LuaInterpreter p, Operator o, VarType var) {
		if(o == Operator.EQUALS && var instanceof Function) {
			return new Boolean(((Function)var).function == function);
		} else if(o == Operator.NOT_EQUALS && var instanceof Function) {
			return new Boolean(((Function)var).function != function);
		} else if(o == Operator.NOT_EQUALS) {
			return new Boolean(true);
		}
		if(o == Operator.CONCATENATE) {
			return new StringType(getValueString() + var.getValueString());
		}

		p.throwError("Operator " + o + " not valid for function and " + var, 0);
		return null;
	}
	
	public Table callFunction(LuaInterpreter p, VarType[] params) {
		Call c = new Call(name, function, params);
		
		return p.functionCall(c);
	}
	
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "function";
	}
	
	public String getValueString() {
		return "function: " + Integer.toHexString(hashCode());
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Function) {
			return ((Function)o).function == function;
		}
		return false;
	}
	
	public boolean isTrue() {
		return true;
	}

	public Number asNumber(LuaInterpreter p) {
		p.throwError(getValueString() + " does not represent a number value", 0);
		
		return null;
	}
}
