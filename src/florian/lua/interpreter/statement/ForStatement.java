package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.VarType;
import florian.lua.interpreter.types.Number;

public class ForStatement extends Statement {

	private String var;
	private double max;
	private double increase;
	private Statement[] statements;
	private Term start;

	public ForStatement(Statement s, int line, String fileName, String var, Term start, String s1, String s2) {
		super(s, line, fileName);
		this.var = var;
		this.start = start;
		this.max = Double.parseDouble(s1.trim());
		this.increase = Double.parseDouble(s2.trim());
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		VarType v = start.computeValue(p, this).unpack();
		createLocalVar(var, v);
		if(!(v instanceof Number))
			return null;
		
		for(double i = ((Number)v).getValue();increase>0?i <= max:i >= max;i += increase) {
			createLocalVar(var, new Number(i));
			for(Statement s:statements) {
				VarType[] vs = s.execute(p);
				if(vs != null)
					return vs;
			}
		}
		
		return null;
	}

	public void setStatements(Statement[] statements) {
		this.statements = statements;
	}

}
