package florian.lua.interpreter.types;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;

public class Boolean extends VarType {
	private boolean value;

	public Boolean(boolean value) {
		this.value = value;
	}

	@Override
	public VarType operator(LuaInterpreter p, Operator o, VarType var) {
		if(o == Operator.CONCATENATE) {
			return new StringType(getValueString() + var.getValueString());
		}
		if(o == Operator.EQUALS && var instanceof Boolean) {
			return new Boolean(value == ((Boolean)var).value);
		} else if(o == Operator.NOT_EQUALS && var instanceof Boolean) {
			return new Boolean(value != ((Boolean)var).value);
		}

		p.throwError("Operator " + o + " not valid for boolean and " + var, 0);
		return null;
	}
	
	public String toString() {
		return "boolean";
	}

	public boolean getValue() {
		return value;
	}
	
	public String getValueString() {
		return String.valueOf(value);
	}
	
	public int hashCode() {
		return value?2:1;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Boolean) {
			return ((Boolean)o).value == value;
		}
		return false;
	}
	
	public boolean isTrue() {
		return value;
	}

	public Number asNumber(LuaInterpreter p) {
		p.throwError(getValueString() + " does not represent a number value", 0);
		
		return null;
	}
}
