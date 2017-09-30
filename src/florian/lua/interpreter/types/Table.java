package florian.lua.interpreter.types;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;

public class Table extends VarType {
	private HashMap<VarType, VarType> table = new HashMap<VarType, VarType>();

	public Table(VarType... types) {
		for(int i = 0; i < types.length; i++) {
			table.put(new Number(i + 1), types[i]);
		}
	}
	
	public Table() {
	}

	@Override
	public VarType operator(LuaInterpreter p, Operator o, VarType var) {
		if(o == Operator.EQUALS && var instanceof Table) {
			return new Boolean(((Table)var).table.equals(table));
		}
		if(o == Operator.NOT_EQUALS && var instanceof Table) {
			return new Boolean(!((Table)var).table.equals(table));
		} else  if(o == Operator.NOT_EQUALS) {
			return new Boolean(true);
		}
		if(o == Operator.CONCATENATE) {
			return new StringType(getValueString() + var.getValueString());
		}
		if(o == Operator.LENGTH) {
			return new Number(getLength());
		}
		p.throwError("Operator " + o + " not valid for table and " + var, 0);
		return null;
	}
	
	public VarType getValue(VarType key) {
		VarType type = table.get(key);
		
		return type == null?new NilValue():type;
	}

	public boolean isValueSet(VarType key) {
		return table.get(key) != null;
	}
	
	public void setValue(VarType key, VarType value) {
		table.put(key, value);
	}

	@Override
	public String toString() {
		return "table";
	}
	
	public String getValueString() {
		String ret = "[";
		Set<VarType> set = new TreeSet<VarType>(table.keySet());
		for(VarType v:set) {
			ret += v.getValueString() + "=" + table.get(v).getValueString() + ",";
		}
		if(table.size() > 0)
			ret = ret.substring(0, ret.length() - 1);
		return ret + "]";
	}
	
	public VarType unpack() {
		if(size(table.keySet()) <= 1 && table.containsKey(new Number(1))) {
			return table.get(new Number(1));
		}
		return this;
	}
	
	private <T> int size(Set<T> set) {
		int size = 0;
		for(T t:set) {
			if(!(table.get(t) instanceof NilValue)) {
				size++;
			}
		}
		
		return size;
	}
	
	public int hashCode() {
		return table.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Table) {
			return ((Table)o).table.equals(table);
		}
		return false;
	}
	
	public boolean isTrue() {
		return true;
	}

	public int size() {
		return table.size();
	}

	public Set<VarType> getKeys() {
		return table.keySet();
	}

	public int getLength() {
		int i = 1;
		while(true) {
			if(getValue(new Number(i)) instanceof NilValue)
				return i - 1;
			i++;
		}
	}


	public Number asNumber(LuaInterpreter p) {
		p.throwError(getValueString() + " does not represent a number value", 0);
		
		return null;
	}
}
