package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.error.LuaContinueException;
import florian.lua.interpreter.types.VarType;

public class ContinueStatement extends Statement {

	public ContinueStatement(Statement s, int line, String fileName) {
		super(s, line, fileName);
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		throw new LuaContinueException();
	}

}
