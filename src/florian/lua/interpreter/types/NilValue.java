package florian.lua.interpreter.types;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;

public class NilValue extends VarType {

	@Override
	public VarType operator(LuaInterpreter p, Operator o, VarType var) {
		if(o == Operator.EQUALS && var instanceof NilValue) {
			return new Boolean(true);
		} else if(o == Operator.NOT_EQUALS && var instanceof NilValue) {
			return new Boolean(false);
		} else if(o == Operator.NOT_EQUALS) {
			return new Boolean(true);
		}
		if(o == Operator.CONCATENATE) {
			return new StringType(getValueString() + var.getValueString());
		}

		p.throwError("Operator " + o + " not valid for nil and " + var, 0);
		return null;
	}

	@Override
	public String toString() {
		return "nil";
	}
	
	public String getValueString() {
		return "nil";
	}
	
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof NilValue;
	}
	
	public boolean isTrue() {
		return false;
	}
	

	public Number asNumber(LuaInterpreter p) {
		p.throwError(getValueString() + " does not represent a number value", 0);
		
		return null;
	}
}
