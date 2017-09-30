package florian.lua.modules;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import florian.lua.annotations.LuaCallable;
import florian.lua.annotations.LuaModuleName;
import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.error.LuaError;
import florian.lua.interpreter.types.Boolean;
import florian.lua.interpreter.types.Function;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.StringType;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

@LuaModuleName(name = "")
public class BasicModule {
	
	@LuaCallable
	public Table dofile(LuaInterpreter p, VarType fileName) {
		InputStream stream = null;
		String name = "";
		
		if(!(fileName instanceof NilValue)) {
			try {
				stream = new FileInputStream(new File(fileName.getValueString()));
				name = fileName.getValueString();
			} catch (FileNotFoundException e) {
				p.throwError("file not found", 0);
			}
		} else {
			stream = p.getInput();
			name = "command Line";
		}
		
		String code = "";
		Table t;
		do {
			code = "";
			try {
				int c = stream.read();
				do {
					code += (char) c;
				} while((stream.available() > 0 && (c = stream.read()) != -1) || c != '\n');
			} catch (IOException e) {
				p.throwError("failed to read: " + e.getMessage(), 0);
			}
		} while(!(t = p.run(name, code)).isValueSet(new Number(1)));
		
		return t;
	}
	
	@LuaCallable
	public Table print(LuaInterpreter p, VarType value) {
		p.getOutput().print(value.getValueString());
		
		return new Table();
	}

	@LuaCallable
	public Table error(LuaInterpreter p, VarType msg, VarType level) {
		int lvl = 1;
		if(level instanceof Number) {
			double val = ((Number)level).getValue();
			
			if(((Number)level).isInteger())
				lvl = (int)val;
			else
				p.throwError(val + "is no integer", 0);
		}
		
		p.throwError(msg.getValueString(), lvl);
		
		return new Table();
	}
	
	@LuaCallable
	public Table pairs(LuaInterpreter p, VarType arg) {
		Table t = new Table();
		
		if(!(arg instanceof Table))
			p.throwError("Argument is not of type table", 1);
		
		Table table = (Table)arg;
		int i = 1;
		for(VarType v:table.getKeys()) {
			t.setValue(new Number(i++), v);
			t.setValue(new Number(i++), table.getValue(v));
		}
		
		return t;
	}
	
	@LuaCallable
	public Table pcall(LuaInterpreter p, VarType function, VarType... params) {
		try{
			if(function instanceof Function) {
				Table t2 = ((Function)function).callFunction(p, params);

				Table t = new Table(new Boolean(true));
				for(int i = 0; i < t2.size(); i++) {
					t.setValue(new Number(i + 2), t2.getValue(new Number(i + 1)));
				}
				
				return t;
			}
		} catch(LuaError e) {
			return new Table(new Boolean(false));
		}
		p.throwError(function + " is not a function", 0);
		
		return null;
	}
	
	@LuaCallable
	public Table tonumber(LuaInterpreter p, VarType num, VarType baseParam) {
		if(num instanceof Number)
			return new Table(num);
		
		Table table = new Table();
		
		int base = 10;
		if(baseParam instanceof Number) {
			double val = ((Number)baseParam).getValue();
			if(((Number)baseParam).isInteger())
				base = (int) val;
			else
				p.throwError(val + " is no integer", 0);
		}
		
		try{
			table.setValue(new Number(1), new Number(Integer.parseInt(num.getValueString(), base)));
		} catch(NumberFormatException e) {
		}
		
		return table;
	}
	
	@LuaCallable
	public Table tostring(LuaInterpreter p, VarType arg) {
		return new Table(new StringType(arg.getValueString()));
	}
	
	@LuaCallable
	public Table type(LuaInterpreter p, VarType type) {
		return new Table(new StringType(type.toString()));
	}
	
	@LuaCallable
	public Table unpack(LuaInterpreter p, VarType list, VarType i, VarType j) {
		Table table = new Table();
		
		if(!(list instanceof Table))
			p.throwError(list.getValueString() + "is not a table", 0);

		if(i instanceof NilValue)
			i = new Number(1);
		else
			i = i.asNumber(p);

		if(j instanceof NilValue)
			j = new Number(((Table)list).getLength());
		else
			j = j.asNumber(p);
		
		int start = (int) ((Number)i).getValue();
		int end = (int) ((Number)j).getValue();
		
		if(!((Number)i).isInteger())
			p.throwError(start + " is no integer", 0);
		
		if(!((Number)j).isInteger())
			p.throwError(end + " is no integer", 0);
		
		for(int pos = start; pos <= end; pos++) {
			table.setValue(new Number(pos), ((Table)list).getValue(new Number(pos)));
		}
		
		return table;
	}
	
	@LuaCallable
	public Table xpcall(LuaInterpreter p, VarType function, VarType errorHandler) {
		try{
			if(function instanceof Function) {
				Table t2 = ((Function)function).callFunction(p, new VarType[0]);

				Table t = new Table(new Boolean(true));
				for(int i = 0; i < t2.size(); i++) {
					t.setValue(new Number(i + 2), t2.getValue(new Number(i + 1)));
				}
				
				return t;
			}
		} catch(LuaError e) {
			if(errorHandler instanceof Function) {
				Table t2 = ((Function)errorHandler).callFunction(p, new VarType[0]);
	
				Table t = new Table(new Boolean(false));
				for(int i = 0; i < t2.size(); i++) {
					t.setValue(new Number(i + 2), t2.getValue(new Number(i + 1)));
				}
				
				return t;
			}
			p.throwError(errorHandler + " is not a function", 0);
		}
		p.throwError(function + " is not a function", 0);
		
		return null;
	}
}
