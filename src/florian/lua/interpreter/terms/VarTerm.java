package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;

public class VarTerm extends Term {

	private String name;
	public VarTerm(String name) {
		this.name = name;
	}
	
	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		Table t = new Table();
		t.setValue(new Number(1), s.getVar(name));
		return t;
	}

}
