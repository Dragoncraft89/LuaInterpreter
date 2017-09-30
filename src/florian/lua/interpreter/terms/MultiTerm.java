package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;

public class MultiTerm extends Term {

	private Term[] terms;

	public MultiTerm(Term... terms) {
		this.terms = terms;
	}

	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		Table t = new Table();
		
		for(int i = 0; i < terms.length; i++) {
			t.setValue(new Number(i + 1), terms[i].computeValue(p, s).unpack());
		}
		
		return t;
	}

}
