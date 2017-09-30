package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.types.VarType;

public abstract class Statement {
	
	protected Statement superStatement;
	protected int line;
	protected String fileName;

	public Statement(Statement s, int line, String fileName) {
		this.superStatement = s;
		this.line = line;
		this.fileName = fileName;
	}

	public abstract VarType[] execute(LuaInterpreter p);
	
	public void createLocalVar(String name, VarType value) {
		superStatement.createLocalVar(name, value);
	}
	
	public VarType getVar(String name) {
		return superStatement.getVar(name);
	}

	public boolean hasGlobalVar(String name) {
		return superStatement.hasGlobalVar(name);
	}
	
	public boolean hasLocalVar(String name) {
		return superStatement.hasLocalVar(name);
	}
	
	public void createGlobalVar(String name, VarType value) {
		superStatement.createGlobalVar(name, value);
	}
	
	public void createVar(String name, VarType value) {
		if(!hasLocalVar(name) && superStatement.hasGlobalVar(name)) {
			superStatement.createGlobalVar(name, value);
			return;
		}
		
		createLocalVar(name, value);
	}
	
	public String getLine() {
		return fileName + " on line: " + String.valueOf(line);
	}
}
