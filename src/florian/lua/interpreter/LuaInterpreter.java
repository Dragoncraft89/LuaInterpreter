package florian.lua.interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import florian.lua.interpreter.functions.Call;
import florian.lua.interpreter.parser.Parser;
import florian.lua.interpreter.parser.Token;
import florian.lua.interpreter.statement.FunctionStatement;
import florian.lua.interpreter.statement.LuaCallableFunction;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.terms.Term;
import florian.lua.interpreter.types.Function;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.StringType;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;
import florian.lua.modules.BasicModule;
import florian.lua.Output;
import florian.lua.annotations.LuaCallable;
import florian.lua.annotations.LuaModuleName;
import florian.lua.interpreter.error.Error;
import florian.lua.interpreter.error.LuaBreakException;
import florian.lua.interpreter.error.LuaContinueException;
import florian.lua.interpreter.error.LuaError;
import florian.lua.interpreter.error.LuaParserException;

public class LuaInterpreter extends FunctionStatement{
	public static String STRING_VERSION = "Custom Lua 1.0";
	
	//Program's output stream
	private Output output = new Output();
	//Program's input (command line)
	private InputStream input = System.in;
	
	//Callstack
	private Stack<Call> stack = new Stack<Call>();
	
	//gloal and local var lookup tables
	private HashMap<String, VarType> global = new HashMap<String, VarType>();
	private HashMap<String, VarType> local = new HashMap<String, VarType>();
	
	private boolean running;
	private int line;
	
	public LuaInterpreter() {
		//subclass of FunctionStatement, used for exception-handling
		super(null, 0, "Interpreter", null, null, "Interpreter", false);
		
		loadDefaultLibraries();
		
		createGlobalVar("_VERSION", new StringType(STRING_VERSION));
	}
	
	/**
	 * Runs lua code
	 * @param programName
	 * @param code
	 * @return
	 */
	public Table run(String programName, String code) {
		//backing up vars, if a file calls another file
		String backupName = fileName;
		HashMap<String, VarType> backupLocal = local;
		int backupLine = line;
		
		//init new vars
		local = new HashMap<String, VarType>();
		fileName = programName;
		
		//reformat code, strip out unimportant information
		code = code.replace("\r", "").replace("\t", " ").replace(";", " ");
		
		//splitting lines
		String[] lines = code.split("\n");
		
		ArrayList<Token> strings = new ArrayList<Token>();
		
		int lineNumber = 0;
		for(String l:lines) {
			lineNumber++;
			strings.addAll(Arrays.asList(Token.toArray(l.split(" "), lineNumber, fileName)));
		}
		
		//push the Interpreter Function call onto the stack
		Call call = new Call(programName, this, new VarType[0]);
		stack.push(call);
		Parser parser = new Parser(strings.toArray(new Token[strings.size()]));
		Statement[] parsed = parser.getStatements(this);
		
		running = true;
		line = 0;
		
		Table t = new Table();
		try {
			while(running) {
				if(this.line >= parsed.length) {
					break;
				}
				
				Statement s = parsed[line];
				line++;
				try {
					VarType[] v = s.execute(this);
					
					if(v == null)
						continue;
					
					for(int x = 0; x < v.length; x++) {
						t.setValue(new Number(x + 1), v[x]);
					}
					break;
					//break and continue are implemented by exceptions
				} catch(LuaBreakException e) {
					output.printError("break not inside code block: " + s.getLine());
				} catch(LuaContinueException e) {
					output.printError("continue not inside loop: " + s.getLine());
				}
			}
		} catch(LuaError error) {
			error.printStacktrace(this);
			terminate();
		} catch(LuaParserException e) {
			throw e;
		} catch(Throwable throwable) {
			throwable.printStackTrace();
		}
		
		//clear up and moving backed up vars back in
		local.clear();
		stack.pop();
		
		line = backupLine;
		fileName = backupName;
		local = backupLocal;
		
		return t;
	}
	
	/**runs a file
	 * 
	 * @param file
	 */
	public void run(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String code = "";
			String line = null;
			while((line = reader.readLine()) != null) {
				code += line + "\n";
			}
			run(file.getName(), code);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**returns the current file + line output string (used for error output)
	 */
	public String getLine() {
		return fileName + " on line: " + line;
	}

	public Output getOutput() {
		return output;
	}

	public InputStream getInput() {
		return input;
	}

	/**throws a lua error
	 * 
	 * @param string
	 * @param i
	 */
	public void throwError(String string, int i) {
		Call[] stacktrace = stack.toArray(new Call[stack.size()]);
		stacktrace = reverse(stacktrace);
		stacktrace = Arrays.copyOfRange(stacktrace, i, stacktrace.length);
		
		throw new LuaError(new Error(string, stacktrace));
	}
	
	/**
	 * reverses an array, e.g. [0,1,2] -> [2,1,0]
	 * @param c
	 * @return
	 */
	private <T> T[] reverse(T[] c) {
		T[] t = Arrays.copyOf(c, c.length);
		
		for(int i = 0; i < c.length; i++) {
			t[c.length - i - 1] = c[i];
		}
		
		return t;
	}
	
	/**
	 * terminates the interpreter
	 */
	public void terminate() {
		running = false;
	}
	
	/**
	 * Calls a function (internal use only)
	 * @param c
	 * @return
	 */
	public Table functionCall(Call c) {
		try{
			stack.push(c);
			Table table = ((FunctionStatement)c.getFunction()).callFunction(this, c.getParams());
			stack.pop();
			return table;
		} catch(LuaError e) {
			stack.pop();
			throw e;
		}
	}

	public void createLocalVar(String name, VarType var) {
		local.put(name, var);
	}
	
	public void createVar(String name, VarType value) {
		if(local.containsKey(name)) {
			createLocalVar(name, value);
		} else {
			createGlobalVar(name, value);
		}
	}
	
	public VarType getVar(String name) {
		VarType type = local.get(name);
		if(type == null)
			type = global.get(name);
		
		if(type == null)
			type = new NilValue();
		
		return type;
	}

	public boolean hasGlobalVar(String name) {
		return global.containsKey(name);
	}
	
	public void createGlobalVar(String name, VarType var) {
		global.put(name, var);
	}

	@Override
	// inherited from FunctionStatement
	public VarType[] execute(LuaInterpreter p) {
		throw new RuntimeException("This method should not be called");
	}
	
	private void loadDefaultLibraries() {
		loadModule(new BasicModule());
		
		run(new File("files/modules.lua"));
	}

	/**
	 * Loads a module<br>
	 * The given object's methods are analyzed through the Java Reflect API and every one that is annotated with {@link LuaCallable} will be converted into a callable lua function<br>
	 * You can give your class a module name with the {@link LuaModuleName} annotation<br>
	 * If no module name is given, the callable functions will be placed into global variable space<br>
	 * <br>
	 * It then can be called in lua: MODULENAME.FUNCTIONNAME()
	 * @param o
	 */
	private void loadModule(Object o) {
		Class<?> c = o.getClass();
		
		LuaModuleName moduleName = c.getDeclaredAnnotation(LuaModuleName.class);
		
		if(!isNameValid(moduleName.name())) {
			throw new LuaParserException("Cannot load module: \'" + moduleName.name() + "\', invalid identifier");
		}
		
		Table table;
		if(moduleName != null && !moduleName.name().equals("") && getVar(moduleName.name()) instanceof Table) {
			table = (Table) getVar(moduleName.name());
		} else {
			table = new Table();
		}
		
		Method[] methods = c.getDeclaredMethods();
		
		for(Method m:methods) {
			if(m.isAnnotationPresent(LuaCallable.class)) {
				LuaCallableFunction func = new LuaCallableFunction(this, m, o);
				VarType name = new StringType(func.getName());
				VarType type = new Function(func.getName(), func);
				
				table.setValue(name, type);
			}
		}

		if(moduleName != null && !moduleName.name().equals(""))
			global.put(moduleName.name(), table);
		else
			for(VarType v:table.getKeys()) {
				global.put(v.getValueString(), table.getValue(v));
			}
	}

	/**
	 * Checks if a name is a valid lua identifier
	 * @param name
	 * @return
	 */
	private boolean isNameValid(String name) {
		return (name == null || name.equals("") || name.matches("^[a-zA-Z]+?[0-9a-zA-Z]*$")) && !Term.isKeyword(name);
	}
}
