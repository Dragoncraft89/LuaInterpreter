package florian.lua.interpreter.types;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;

public class Number extends VarType {

	private double value;
	
	public Number(double value) {
		this.value = value;
	}
	
	@Override
	public VarType operator(LuaInterpreter p, Operator o, VarType var) {
		if(o == Operator.ADD && var instanceof Number) {
			return new Number(value + ((Number)var).value);
		}
		if(o == Operator.SUBSTRACT && var instanceof Number) {
			return new Number(value - ((Number)var).value);
		}
		if(o == Operator.MULTIPLY && var instanceof Number) {
			return new Number(value * ((Number)var).value);
		}
		if(o == Operator.DIVIDE && var instanceof Number) {
			return new Number(value / ((Number)var).value);
		}
		
		if(o == Operator.EQUALS && var instanceof Number) {
			return new Boolean(value == ((Number)var).value);
		}
		if(o == Operator.NOT_EQUALS) {
			return new Boolean(!getValueString().equals(var.getValueString()));
		}
		if(o == Operator.LOWER_EQUALS_THAN && var instanceof Number) {
			return new Boolean(value <= ((Number)var).value);
		}
		if(o == Operator.GREATER_EQUALS_THAN && var instanceof Number) {
			return new Boolean(value >= ((Number)var).value);
		}
		if(o == Operator.LOWER_THAN && var instanceof Number) {
			return new Boolean(value < ((Number)var).value);
		}
		if(o == Operator.GREATER_THAN && var instanceof Number) {
			return new Boolean(value > ((Number)var).value);
		}
		if(o == Operator.EXPONENTIATE && var instanceof Number) {
			return new Number(Math.pow(value, ((Number)var).value));
		}
		if(o == Operator.CONCATENATE) {
			return new StringType(getValueString() + var.getValueString());
		}

		p.throwError("Operator " + o + " not valid for number and " + var, 0);
		return null;
	}

	public boolean isInteger() {
		return value == (int)value && !Double.isInfinite(value);
	}
	
	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "number";
	}

	public String getValueString() {
		if((int)value == value)
			return String.valueOf((int)value);
		return String.valueOf(value);
	}

	public int hashCode() {
		return (int) (value * 23);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Number) {
			return ((Number)o).value == value;
		}
		return false;
	}
	
	public boolean isTrue() {
		return true;
	}

	
	public Number asNumber(LuaInterpreter p) {
		return this;
	}
}
