package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Table;

public class NullTerm extends Term {

	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		return new Table(new NilValue());
	}

}
