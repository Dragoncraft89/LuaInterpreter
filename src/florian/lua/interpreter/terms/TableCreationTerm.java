package florian.lua.interpreter.terms;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;

public class TableCreationTerm extends Term {

	private Term[] content;
	private Term[] names;

	public TableCreationTerm(Term[] names, Term[] content) {
		this.names = names;
		this.content = content;
	}

	@Override
	public Table computeValue(LuaInterpreter p, Statement s) {
		Table table = new Table();
		int counter = 0;
		for(int i = 0; i < content.length; i++) {
			if(i < names.length && names[i] != null)
				table.setValue(names[i].computeValue(p, s).unpack(), content[i].computeValue(p, s).unpack());
			else if(content[i] != null)
				table.setValue(new Number(++counter), content[i].computeValue(p, s).unpack());
		}
		return new Table(table);
	}

}
