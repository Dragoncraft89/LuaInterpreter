package florian.lua.interpreter.types;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;

public abstract class VarType implements Comparable<VarType>{
	
	public int compareTo(VarType o) {
		return getValueString().compareTo(o.getValueString());
	}
	
	public abstract VarType operator(LuaInterpreter p, Operator o, VarType var);
	
	public abstract String toString();

	public abstract String getValueString();
	
	public abstract int hashCode();
	
	public abstract boolean equals(Object o);
	
	public abstract boolean isTrue();
	
	public abstract Number asNumber(LuaInterpreter p);
}
