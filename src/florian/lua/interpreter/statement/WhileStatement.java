package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.error.LuaBreakException;
import florian.lua.interpreter.error.LuaContinueException;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.VarType;

public class WhileStatement extends Statement {

	private Term condition;
	private Statement[] statements;

	public WhileStatement(Statement superStatement, int line, String fileName, Term condition, Statement[] statements) {
		super(superStatement, line, fileName);
		this.condition = condition;
		this.statements = statements;
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		while(condition.computeValue(p, this).unpack().isTrue()) {
			try {
				exec(statements, p);
			} catch(LuaBreakException e) {
				break;
			} catch(LuaContinueException e) {
			}
		}
		return null;
	}
	
	private VarType[] exec(Statement[] s, LuaInterpreter p) {
		for(Statement x:s) {
			VarType[] v = x.execute(p);
			if(v != null) {
				return v;
			}
		}
		return null;
	}

	public void setStatements(Statement[] s) {
		this.statements = s;
	}

}
