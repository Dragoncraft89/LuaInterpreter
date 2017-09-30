package florian.lua.interpreter.types;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;

public class StringType extends VarType{

	private String value;

	public StringType(String value) {
		this.value = value;
	}
	
	@Override
	public VarType operator(LuaInterpreter p, Operator o, VarType var) {
		if(o == Operator.CONCATENATE) {
			return new StringType(value + var.getValueString());
		}
		if(o == Operator.EQUALS && var instanceof StringType) {
			return new Boolean(value.equals(((StringType)var).value));
		}
		if(o == Operator.NOT_EQUALS && var instanceof StringType) {
			return new Boolean(!value.equals(((StringType)var).value));
		} else if(o == Operator.NOT_EQUALS) {
			return new Boolean(true);
		}
		if(o == Operator.LENGTH) {
			return new Number(value.length());
		}
		
		if(isNumeric(value) && var instanceof StringType && isNumeric(((StringType)var).value)) {
			Number v1 = new Number(Double.parseDouble(value));
			Number v2 = new Number(Double.parseDouble(((StringType)var).value));
			
			return v1.operator(p, o, v2);
		}
		
		p.throwError("Operator " + o + " not valid for string and " + var, 0);
		return null;
	}

	@Override
	public String toString() {
		return "string";
	}
	
	public String getValueString() {
		return value;
	}
	
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof StringType) {
			return ((StringType)o).value.equals(value);
		}
		return false;
	}

	public boolean isTrue() {
		return true;
	}
	
	private boolean isNumeric(String term) {
		try {
			Double.parseDouble(term);
			return true;
		} catch(NumberFormatException e) {
		}
		return false;
	}
	
	public Number asNumber(LuaInterpreter p) {
		try {
			return new Number(Double.parseDouble(value));
		} catch(NumberFormatException e) {
			p.throwError(getValueString() + " does not represent a number value", 0);
		}
		
		return null;
	}
}
