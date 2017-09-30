package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.parser.Parser;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class AssignmentStatement extends Statement {

	private String[] var;
	private Term[] index;
	private Term term;
	private boolean local;

	public AssignmentStatement(Statement s, int line, String fileName, String[] var, Term term, boolean local, Parser parser) {
		super(s, line, fileName);
		this.var = new String[var.length];
		this.index = new Term[var.length];
		for(int i = 0; i < var.length; i++) {
			if(Term.isTable(var[i])) {
				int pos = var[i].indexOf("[");
				this.var[i] = var[i].substring(0, pos).trim();
				this.index[i] = Term.createFromString(var[i].substring(pos + 1, var[i].length() - 1), parser);
			} else {
				this.var[i] = var[i];
			}
		}
		this.term = term;
		this.local = local;
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		Table t = term.computeValue(p, this);
		for(int i = 0; i < var.length; i++) {
			if(index[i] == null) {
				if(local)
					superStatement.createLocalVar(var[i], t.getValue(new Number(i + 1)));
				else
					superStatement.createVar(var[i], t.getValue(new Number(i + 1)));
			} else {
				VarType v = superStatement.getVar(var[i]);
				if(v instanceof Table) {
					((Table)v).setValue(index[i].computeValue(p, this).unpack(), t.getValue(new Number(i + 1)));
				} else {
					p.throwError("Invalid assertion: " + var[i] + " is " + v + " not table", 0);
				}
			}
		}
		return null;
	}

}
