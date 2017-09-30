package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class TableTerm extends Term {
	
	private Term var;
	private Term index;

	public TableTerm(Term var, Term index) {
		this.var = var;
		this.index = index;
	}

	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		VarType table = var.computeValue(p, s).unpack();
		
		if(table instanceof Table) {
			Table t = new Table();
			t.setValue(new Number(1), ((Table)table).getValue(index.computeValue(p, s).unpack()));
			return t;
		} else {
			p.throwError(table + " is not a table", 0);
		}
		
		return null;
	}

}
