package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Function;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class FunctionTerm extends Term {

	private Term var;
	private Term[] par;

	public FunctionTerm(Term var, Term[] par) {
		this.var = var;
		this.par = par;
	}

	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		VarType v = var.computeValue(p, s).getValue(new Number(1));
		if(v instanceof Function) {
			VarType[] params = new VarType[par.length];
			for(int i = 0; i < params.length; i++) {
				params[i] = par[i].computeValue(p, s).unpack();
			}
			return ((Function)v).callFunction(p, params);
		}
		p.throwError("Cannot call " + v, 0);
		
		return new Table(new NilValue());
	}

}
