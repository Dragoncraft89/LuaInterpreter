package florian.lua.test;

import java.io.File;

import florian.lua.interpreter.LuaInterpreter;

public class LuaTest {

	public static void main(String[] args) {
		LuaInterpreter p = new LuaInterpreter();
		
		p.run(new File("test.lua"));
	}

}
