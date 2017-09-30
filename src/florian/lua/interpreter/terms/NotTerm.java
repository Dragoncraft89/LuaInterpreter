package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Boolean;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;

public class NotTerm extends Term {

	private Term term;

	public NotTerm(Term term) {
		this.term = term;
	}

	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		Table table = new Table();
		table.setValue(new Number(1), new Boolean(!term.computeValue(p, s).unpack().isTrue()));
		return table;
	}

}
