package florian.lua.interpreter.statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.error.InvalidLuaMethodException;
import florian.lua.interpreter.statement.FunctionStatement;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class LuaCallableFunction extends FunctionStatement {

	private Method method;
	private Object object;

	public LuaCallableFunction(LuaInterpreter p, Method m, Object o) {
		super(p, -1, "Java", new Statement[0], new String[0], m.getName(), false);
		
		this.method = m;
		this.object = o;
		Parameter[] parameters = method.getParameters();

		//checks for valid functions
		if(!parameters[0].getType().equals(LuaInterpreter.class))
			throw new InvalidLuaMethodException("A method marked as @LuaCallable must have a LuaInterpreter type as first argument");

		for(int i = 1; i < parameters.length; i++)
			if(!(parameters[i].getType().isAssignableFrom(VarType.class) || parameters[i].getType().isAssignableFrom(VarType[].class)))
				throw new InvalidLuaMethodException("A method marked as @LuaCallable must have only VarType or Object types as their arguments");

		if(!Table.class.isAssignableFrom(method.getReturnType()))
			throw new InvalidLuaMethodException("A method marked as @LuaCallable must return a Table object");
		
	}
	
	/**
	 * loads the params and calls the java function
	 */
	public void call(LuaInterpreter p, VarType[] params, Table t) {
		Parameter[] parameters = method.getParameters();
		
		Object[] javaParams = new Object[parameters.length];
		javaParams[0] = p;
		
		for(int i = 1; i < parameters.length; i++) {
			if(!parameters[i].isVarArgs())
				if(i - 1 < params.length && i < javaParams.length)
					//copy the params to a lower index(index 1 is always the LuaInterpreter)
					javaParams[i] = params[i - 1];
				else if(i < javaParams.length)
					//fill up remaining space with nil
					javaParams[i] = new NilValue();
			else {
				VarType[] array;
				if(params.length - i > 1)
					array = new VarType[params.length - i - 1];
				else
					array = new VarType[0];
				for(int arrayIndex = 0; arrayIndex < array.length; arrayIndex++) {
					array[arrayIndex] = params[i - 1];
				}
				javaParams[i] = array;
			}
		}
		
		try {
			//invoke the method
			Table t2 = ((Table)method.invoke(object, javaParams));
			
			for(VarType v:t2.getKeys()) {
				t.setValue(v, t2.getValue(v));
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if(e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException)e.getTargetException();
			}
			e.getTargetException().printStackTrace();
		}
	}
	
	public String getLine() {
		return "Java source";
	}
}
