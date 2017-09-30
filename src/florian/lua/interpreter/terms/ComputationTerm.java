package florian.lua.interpreter.terms;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;
import florian.lua.interpreter.types.Number;

public class ComputationTerm extends Term {

	private Term term1;
	private Term term2;
	private Operator o;

	public ComputationTerm(Term term1, Term term2, Operator o) {
		this.term1 = term1;
		this.term2 = term2;
		this.o = o;
	}
	
	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		Table table = new Table();
		if(o == Operator.OR) {
			VarType v1 = term1.computeValue(p, s).unpack();
			if(v1.isTrue()) {
				table.setValue(new Number(1), v1);
			} else {
				table.setValue(new Number(1), term2.computeValue(p, s).unpack());
			}
		} else if(o == Operator.AND) {
			VarType v1 = term1.computeValue(p, s).unpack();
			if(!v1.isTrue()) {
				table.setValue(new Number(1), v1);
			} else {
				table.setValue(new Number(1), term2.computeValue(p, s).unpack());
			}
		} else {
			table.setValue(new Number(1), term1.computeValue(p, s).getValue(new Number(1)).operator(p, o, term2.computeValue(p, s).unpack()));
		}
		return table;
	}

}
