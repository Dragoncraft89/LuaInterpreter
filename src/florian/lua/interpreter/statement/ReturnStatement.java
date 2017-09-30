package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class ReturnStatement extends Statement{

	private Term values;

	public ReturnStatement(Statement superStatement, int line, String fileName, Term term) {
		super(superStatement, line, fileName);
		this.values = term;
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		Table t = values.computeValue(p, this);
		
		if(t.getLength() == 0) {
			return new VarType[]{new NilValue()};
		}
		
		VarType[] vars = new VarType[t.getLength()];
		for(int i = 0; i < t.getLength(); i++) {
			vars[i] = t.getValue(new Number(i + 1));
		}
		return vars;
	}

}
