package florian.lua.interpreter.statement;

import java.util.Iterator;
import java.util.TreeSet;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class ForEachStatement extends Statement {

	private String[] varName;
	private Term term;
	
	private Statement[] statements;

	public ForEachStatement(Statement superStatement, int line, String fileName, String[] varName,
			Term term) {
		super(superStatement, line, fileName);
		
		this.varName = varName;
		this.term = term;
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		VarType compute = term.computeValue(p, this).unpack();
		if(!(compute instanceof Table))
			return null;
		
		Table values = (Table) compute;
		
		Iterator<VarType> iterator = new TreeSet<VarType>(values.getKeys()).iterator();
		
		while(iterator.hasNext()) {
			VarType v = iterator.next();
			if(varName.length > 0) {
				createVar(varName[0], values.getValue(v));
				
				if(varName.length > 1) {
					createVar(varName[1], values.getValue(iterator.next()));
				}
			}

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
