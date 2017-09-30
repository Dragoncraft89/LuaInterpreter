package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.VarType;

public class IfStatement extends Statement {

	private Statement[] t;
	private Term condition;
	private Statement[] f;

	public IfStatement(Statement superStatement, int line, String fileName, Term condition, Statement[] t, Statement[] f) {
		super(superStatement, line, fileName);
		this.condition = condition;
		this.t = t;
		this.f = f;
	}
	
	public IfStatement(Statement superStatement, int line, String fileName, Term condition, Statement[] t) {
		super(superStatement, line, fileName);
		this.condition = condition;
		this.t = t;
		this.f = new Statement[0];
	}
	
	@Override
	public VarType[] execute(LuaInterpreter p) {
		if(condition.computeValue(p, this).unpack().isTrue()) {
			return exec(t, p);
		} else {
			return exec(f, p);
		}
	}
	
	private VarType[] exec(Statement[] s, LuaInterpreter p) {
		for(Statement x:s) {
			if(x == null)
				return null;
			VarType[] v = x.execute(p);
			if(v != null) {
				return v;
			}
		}
		return null;
	}

	public void setTrue(Statement[] t) {
		this.t = t;
	}
	
	public void setFalse(Statement[] f) {
		this.f = f;
	}

	public void setCondition(Term condition) {
		this.condition = condition;
	}

}
