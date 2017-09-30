package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.error.LuaBreakException;
import florian.lua.interpreter.types.VarType;

public class BreakStatement extends Statement {

	public BreakStatement(Statement s, int line, String fileName) {
		super(s, line, fileName);
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		throw new LuaBreakException();
	}

}
