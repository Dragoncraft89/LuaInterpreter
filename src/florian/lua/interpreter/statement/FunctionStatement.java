package florian.lua.interpreter.statement;

import java.util.HashMap;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.error.LuaBreakException;
import florian.lua.interpreter.types.Function;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class FunctionStatement extends Statement {

	private HashMap<String, VarType> vars = new HashMap<String, VarType>();
	
	private Statement currentStatement;
	private Statement[] statements;
	private String[] params;
	private String name;
	private boolean local;

	public FunctionStatement(Statement superStatement, int line, String fileName, Statement[] statements, String[] params, String name, boolean local) {
		super(superStatement, line, fileName);
		this.statements = statements;
		this.params = params;
		this.name = name;
		this.local = local;
	}
	
	@Override
	public VarType[] execute(LuaInterpreter p) {
		if(local)
			superStatement.createLocalVar(name, new Function(name, this));
		else
			superStatement.createVar(name, new Function(name, this));
		return null;
	}
	
	public final Table callFunction(LuaInterpreter p, VarType[] params) {
		HashMap<String, VarType> copy = vars;
		vars = new HashMap<String, VarType>();
		for(int i = 0; i < this.params.length; i++) {
			if(i < params.length) {
				createLocalVar(this.params[i], params[i]);
			} else {
				createLocalVar(this.params[i], new NilValue());
			}
		}
		
		Table t = new Table(new NilValue());
		
		for(Statement s:statements) {
			currentStatement = s;
			try {
				VarType[] v = s.execute(p);
				if(v == null)
					continue;
				for(int i = 0; i < v.length; i++) {
					t.setValue(new Number(i + 1), v[i]);
				}
				break;
			} catch(LuaBreakException e) {
				break;
			}
		}
		currentStatement = null;
		
		call(p, params, t);

		vars = copy;
		return t;
	}
	
	protected void call(LuaInterpreter p, VarType[] params, Table t) {
		
	}
	
	public void createLocalVar(String name, VarType value) {
		vars.put(name, value);
	}
	
	public boolean hasLocalVar(String name) {
		return vars.containsKey(name);
	}
	
	public VarType getVar(String name) {
		VarType type = vars.get(name);
		
		if(type == null) {
			return superStatement.getVar(name);
		}
		
		return type;
	}

	public void setStatements(Statement[] statements) {
		this.statements = statements;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getLine() {
		if(currentStatement != null)
			return currentStatement.getLine();
		
		return super.getLine();
	}

}
