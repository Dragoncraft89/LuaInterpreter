package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.VarType;

public class TermStatement extends Statement {

	private Term term;

	public TermStatement(Statement s, int line, String fileName, Term term) {
		super(s, line, fileName);
		this.term = term;
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		term.computeValue(p, this);
		return null;
	}

}
