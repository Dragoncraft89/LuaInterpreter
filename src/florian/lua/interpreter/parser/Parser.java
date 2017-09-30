package florian.lua.interpreter.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import florian.lua.interpreter.error.LuaParserException;
import florian.lua.interpreter.statement.AssignmentStatement;
import florian.lua.interpreter.statement.BreakStatement;
import florian.lua.interpreter.statement.ContinueStatement;
import florian.lua.interpreter.statement.ForEachStatement;
import florian.lua.interpreter.statement.ForStatement;
import florian.lua.interpreter.statement.FunctionStatement;
import florian.lua.interpreter.statement.IfStatement;
import florian.lua.interpreter.statement.ReturnStatement;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.statement.TermStatement;
import florian.lua.interpreter.statement.WhileStatement;
import florian.lua.interpreter.terms.Term;

public class Parser {
	// queue for tokens to be processed
	private LinkedList<Token> tokens;
	private Token currentToken;
	
	public Parser(Token[] tokens) {
		this.tokens = new LinkedList<Token>();
		this.tokens.addAll(Arrays.asList(tokens));
	}
	
	/**
	 * processes a statement
	 * @param superStatement
	 * @return
	 */
	public Statement[] getStatements(Statement superStatement) {
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		while(hasToken()) {
			Statement s = processToken(nextToken(), superStatement);
			
			if(s != null)
				statements.add(s);
		}
		
		return statements.toArray(new Statement[statements.size()]);
	}
	
	private Statement processToken(Token token, Statement superStatement) {
		//strip out comments
		if(token.name.contains("--[[")) {
			int pos = token.name.indexOf("--[[");
			token.name = token.name.substring(0, pos);
			
			while(!peek().name.contains("--]]")) {
				nextToken();
			}
			
			Token peek = peek();

			pos = peek.name.indexOf("--]]");
			peek.name = peek.name.substring(0, pos);
		}
		
		if(token.name.contains("--")) {
			int pos = token.name.indexOf("--");
			token.name = token.name.substring(0, pos);
			skipToLine(token.line + 1);
		}
		
		//does the local keyword appear?
		boolean local = false;
		
		if(token.name.equals("local")) {
			local = true;
			token = nextToken();
		}
		
		//read onward dependend on statement type
		if(token.name.equals("if")) {
			if(local)
				unexpectedToken(token);
			return readIf(superStatement, token.line, token.fileName);
		} else if(token.name.equals("while")) {
			if(local)
				unexpectedToken(token);
			return readWhile(superStatement, token.line, token.fileName);
		} else if(token.name.equals("for")) {
			if(local)
				unexpectedToken(token);
			return readFor(superStatement, token.line, token.fileName);
		} else if(token.name.equals("return")) {
			if(local)
				unexpectedToken(token);
			return readReturn(superStatement, token.line, token.fileName);
		} else if(token.name.equals("break")) {
			if(local)
				unexpectedToken(token);
			return new BreakStatement(superStatement, token.line, token.fileName);
		} else if(token.name.equals("continue")) {
			if(local)
				unexpectedToken(token);
			return new ContinueStatement(superStatement, token.line, token.fileName);
		} else if(token.name.equals("function")) {
			return readFunction(superStatement, token.line, token.fileName, local);
		} else if(!token.name.trim().isEmpty()){
			return readDefault(superStatement, token.line, token.fileName, local);
		}
		
		return null;
	}
	
	/**
	 * Reads a 'normal' statement(assignment, function call,etc)
	 * @param superStatement
	 * @param line
	 * @param fileName
	 * @param local
	 * @return
	 */
	private Statement readDefault(Statement superStatement, int line, String fileName, boolean local) {
		String term = currentToken.name;
		//is it an assignment?
		if((term).contains("=")) {
			int index = term.indexOf("=");

			boolean b = true;
			if(index > 0) {
				char c = term.charAt(index - 1);
				if(c == '~' || c == '<' || c == '>') {
					b = false;
				}
			}
			
			if(b) {
				String left = term.substring(0, index);
				String right = term.substring(index + 1);
				return readAssignment(superStatement, line, fileName, local, left, right);
			}
		}
		
		boolean assignmentPossible = true;
		while(hasToken()) {
			if((term + " " + peek().name).contains("=") && assignmentPossible) {
				term += " " + nextToken().name;
				
				int index = term.indexOf("=");

				if(index > 0) {
					char c = term.charAt(index - 1);
					//is it a comparison?
					if(c == '~' || c == '<' || c == '>') {
						assignmentPossible = false;
						continue;
					}
				}
				
				String left = term.substring(0, index);
				String right = term.substring(index + 1);
				return readAssignment(superStatement, line, fileName, local, left, right);
			}
			
			if(Term.isValid(term) && !Term.isValid(term + " " + peek().name))
				break;
			
			term += " " + nextToken().name;
		}
		
		if(local)
			throw new LuaParserException("Unexpected token: \'" + term + "\'");
		
		return new TermStatement(superStatement, line, fileName, Term.createFromString(term, this));
	}

	private Statement readAssignment(Statement superStatement, int line, String fileName, boolean local, String left,
			String term) {
		
		while(hasToken()) {
			if(Term.isValid(term) && !Term.isValid(term + " " + peek().name))
				break;
			
			term += " " + nextToken().name;
		}
		
		return new AssignmentStatement(superStatement, line, fileName, trim(left.split(",")), Term.createFromString(term, this), local, this);
	}

	private Statement readFunction(Statement superStatement, int line, String fileName, boolean local) {
		Token t = nextToken();
		
		int pos = t.name.indexOf("(");

		String name = pos!=-1?t.name.substring(0, pos):t.name;
		
		if(pos != -1) {
			t.name = t.name.substring(pos + 1);
		} else {
			t = nextToken();
		}
		
		String params = "";
		
		while(!t.name.contains(")")) {
			params += " " + t.name;
			t = nextToken();
		}
		
		int end = t.name.indexOf(")");
		params += t.name.substring(0, end);

		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		FunctionStatement statement = new FunctionStatement(superStatement, line, fileName, null, trim(params.split(",")), name, local);
		
		while(!peek().name.equals("end")) {
			Statement s = processToken(nextToken(), statement);
			
			if(s != null)
				statements.add(s);
		}
		nextToken();
		
		statement.setStatements(statements.toArray(new Statement[statements.size()]));
		
		return statement;
	}

	private Statement readReturn(Statement superStatement, int line, String fileName) {
		String ret = "";
		while(hasToken() && !peek().name.equals("end")) {
			if(Term.isValid(ret) && !Term.isValid(ret + " " + peek().name))
				break;
			
			ret += " " + nextToken().name;
		}
		
		return new ReturnStatement(superStatement, line, fileName, Term.createFromString(ret, this));
	}

	private Statement readFor(Statement superStatement, int line, String fileName) {
		String condition = "";
		
		while(!peek().name.equals("do")) {
			if(peek().name.equals("in")) {
				nextToken();
				return readForEach(superStatement, line, fileName, condition);
			}
			
			condition +=  " " + nextToken().name;
		}
		nextToken();
		
		String[] split = split(condition, ',');
		if(split.length > 3)
			throw new LuaParserException(condition + " is not valid in \'for\'");
		
		int pos = split[0].indexOf("=");
		if(pos == -1)
			expectedToken("=");
		String left = split[0].substring(0, pos).trim();
		Term right = Term.createFromString(split[0].substring(pos + 1, split[0].length()), this);
		
		String param3 = split.length > 2?split[2]:"1";
		ForStatement st = new ForStatement(superStatement, line, fileName, left, right, split[1], param3);
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		while(!peek().name.equals("end")) {
			Statement s = processToken(nextToken(), st);
			
			if(s != null)
				statements.add(s);
		}
		nextToken();
		
		st.setStatements(statements.toArray(new Statement[statements.size()]));
		return st;
	}

	private Statement readForEach(Statement superStatement, int line, String fileName, String varName) {
		String tableTerm = "";
		
		while(!peek().name.equals("do")) {
			tableTerm +=  " " + nextToken().name;
		}
		nextToken();

		ArrayList<Statement> statements = new ArrayList<Statement>();

		ForEachStatement st = new ForEachStatement(superStatement, line, fileName, trim(split(varName, ',')), Term.createFromString(tableTerm, this));

		while(!peek().name.equals("end")) {
			Statement s = processToken(nextToken(), st);
			
			if(s != null)
				statements.add(s);
		}
		nextToken();
		
		st.setStatements(statements.toArray(new Statement[statements.size()]));
		return st;
	}

	private Statement readWhile(Statement superStatement, int line, String fileName) {
		String condition = "";
		
		while(!peek().name.equals("do")) {
			condition += " " + nextToken().name;
		}
		nextToken();
		
		ArrayList<Statement> statements = new ArrayList<Statement>();
		WhileStatement statement = new WhileStatement(superStatement, line, fileName, Term.createFromString(condition, this), null);
		
		while(!peek().name.equals("end")) {
			Statement s = processToken(nextToken(), statement);
			
			if(s != null)
				statements.add(s);
		}
		nextToken();
		
		statement.setStatements(statements.toArray(new Statement[statements.size()]));
		
		return statement;
	}

	private Statement readIf(Statement superStatement, int line, String fileName) {
		String condition = "";
		
		while(!peek().name.equals("then")) {
			condition += " " + nextToken().name;
		}
		
		nextToken();

		ArrayList<Statement> statements = new ArrayList<Statement>();
		IfStatement statement = new IfStatement(superStatement, line, fileName, Term.createFromString(condition, this), null);
		
		while(!peek().name.equals("else") && !peek().name.equals("end")) {
			Statement s = processToken(nextToken(), statement);
			
			if(s != null)
				statements.add(s);
		}
		if(peek().name.equals("else"))
			nextToken();
		
		statement.setTrue(statements.toArray(new Statement[statements.size()]));
		statements.clear();
		
		while(!peek().name.equals("end")) {
			Statement s = processToken(nextToken(), statement);
			
			if(s != null)
				statements.add(s);
		}
		nextToken();
	
		statement.setFalse(statements.toArray(new Statement[statements.size()]));
		return statement;
	}
	
	private void unexpectedToken(Token t) {
		throw new LuaParserException("Unexpected token \'" + t.name + "\' on line: " + t.line);
	}
	
	private void expectedToken(String s) {
		throw new LuaParserException("Expected token \'" + s + "\' on line: " + currentToken.line);
	}
	
	private boolean hasToken() {
		return tokens.size() > 0;
	}
	
	private Token nextToken() {
		if(tokens.isEmpty())
			throw new LuaParserException("Unexpected end of file at:" + currentToken.fileName + " :" + currentToken.line);
		
		currentToken = tokens.removeFirst();
		return currentToken;
	}
	
	private Token peek() {
		return tokens.getFirst();
	}
	
	private void skipToLine(int line) {
		while(peek().line < line) {
			nextToken();
		}
	}
	
	private String[] trim(String[] array) {
		for(int i = 0; i < array.length; i++) {
			array[i] = array[i].trim();
		}
		
		return array;
	}
	
	private String[] split(String term, char split) {
		ArrayList<String> list = new ArrayList<String>();
		
		char[] c = term.toCharArray();
		boolean quotation = false;
		int depth = 0;
		int pos = 0;
		
		for(int i = 0; i < c.length; i++) {
			if(c[i] == split && !quotation && depth == 0) {
				list.add(term.substring(pos, i));
				pos = i + 1;
			}
			if((c[i] == '"' || c[i] == '\'') && (i - 1 == 0 || c[i - 1] != '\\')) {
				quotation = !quotation;
			}
			if(!quotation && c[i] == '(') {
				depth++;
			}
			if(!quotation && c[i] == ')') {
				depth--;
			}
		}
		list.add(term.substring(pos, term.length()));
		
		return list.toArray(new String[list.size()]);
	}
	
	public int getLine() {
		return currentToken.line;
	}
}
