package florian.lua.interpreter.statement;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.Function;
import florian.lua.interpreter.types.VarType;

public class FunctionCall extends Statement {

	private String name;
	private Term[] params;

	public FunctionCall(Statement s, int line, String fileName, String name, Term[] params) {
		super(s, line, fileName);
		this.name = name;
		this.params = params;
	}

	@Override
	public VarType[] execute(LuaInterpreter p) {
		VarType function = p.getVar(name);
		
		if(function instanceof Function) {
			VarType[] params = new VarType[this.params.length];
			
			for(int i = 0; i < params.length; i++) {
				if(this.params[i] != null)
					params[i] = this.params[i].computeValue(p, this).unpack();
			}
			((Function)function).callFunction(p, params);
		} else {
			p.throwError("var \'" + name + "\' is " + function + " not function", 0);
		}
		
		return null;
	}

}
