package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class VarTypeTerm extends Term {

	private VarType value;

	public VarTypeTerm(VarType value) {
		this.value = value;
	}
	
	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		Table table = new Table();
		table.setValue(new Number(1), value);
		return table;
	}

}
