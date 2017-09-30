package florian.lua.interpreter.parser;

public class Token {
	public String name;
	public int line;
	public String fileName;
	
	public Token(String name, int line, String fileName) {
		this.name = name;
		this.line = line;
		this.fileName = fileName;
	}

	public static Token[] toArray(String[] split, int line, String fileName) {
		Token[] tokens = new Token[split.length];
		
		for(int i = 0; i < tokens.length; i++) {
			tokens[i] = new Token(split[i], line, fileName);
		}
		
		return tokens;
	}
}
