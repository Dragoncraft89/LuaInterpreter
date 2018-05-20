# LuaInterpreter
A Interpreter for the programming language Lua, written and accesible from Java.

It comes with the possibility of calling functions directly from Lua, clean and easy.

Examples:
Basic execution of lua files
```
import java.io.File;

import florian.lua.interpreter.LuaInterpreter;

public class LuaTest {

	public static void main(String[] args) {
		LuaInterpreter p = new LuaInterpreter();
		
		p.run(new File("test.lua"));
	}

}
```

Loading custom modules
```
import java.io.File;

import florian.lua.interpreter.LuaInterpreter;
import florian.lua.annotations.LuaCallable;
import florian.lua.annotations.LuaModuleName;
import florian.lua.interpreter.types.Boolean;
import florian.lua.interpreter.types.Table;
import florian.lua.interpreter.types.VarType;

public class LuaTest {
  @LuaModuleName(name="CustomModule")
  public class CustomModule {
    @LuaCallable
    public Table doTest(LuaInterpreter p, VarType argument) {
      p.getOutput().print("module was called!");
      
      return new Table();
    }
  }

  public static void main(String[] args) {
    LuaInterpreter p = new LuaInterpreter();
    p.loadModule(new CustomModule);

    p.run(new File("test.lua"));
  }
}
