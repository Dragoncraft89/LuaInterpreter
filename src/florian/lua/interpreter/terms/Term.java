package florian.lua.interpreter.terms;

import java.util.ArrayList;

import florian.lua.Operator;
import florian.lua.interpreter.LuaInterpreter;
import florian.lua.interpreter.error.LuaParserException;
import florian.lua.interpreter.parser.Parser;
import florian.lua.interpreter.statement.Statement;
import florian.lua.interpreter.types.Boolean;
import florian.lua.interpreter.types.NilValue;
import florian.lua.interpreter.types.Number;
import florian.lua.interpreter.types.StringType;
import florian.lua.interpreter.types.Table;
import florian.lua.parser.SearchObject;

public abstract class Term {
	public Term() {
		
	}
	
	public abstract Table computeValue(LuaInterpreter p, Statement s);
	
	public static Term createFromString(String term, Parser parser) {
		term = term.trim();
		if(isNumeric(term)) {
			return new VarTypeTerm(new Number(Double.parseDouble(term)));
		}
		if(isString(term)) {
			return new VarTypeTerm(toStringType(term));
		}
		if(term.equals("nil")) {
			return new VarTypeTerm(new NilValue());
		}
		if(isBoolean(term)) {
			return new VarTypeTerm(new Boolean(term.equals("true")?true:false));
		}
		if(isVariable(term) && !isKeyword(term)) {
			return new VarTerm(term);
		}
		if(term.matches("^\\{.*\\}$") && getTerms(term, '{', '}') == 1) {
			String[] content = splitFunction(term.substring(1, term.length() - 1), ',');
			Term[] t = new Term[content.length];
			Term[] names = new Term[content.length];
			for(int i = 0; i < content.length; i++) {
				String[] namepair = splitFunction(content[i], '=');
				if(namepair.length == 1 && !namepair[0].isEmpty()) {
					t[i] = createFromString(namepair[0], parser);
				} else if(namepair.length == 2) {
					names[i] = createFromString(namepair[0], parser);
					t[i] = createFromString(namepair[1], parser);
				}
			}
			return new TableCreationTerm(names, t);
		}
		if(isFunction(term)) {
			int openPos = term.indexOf("(");
			int closePos = findEnd(term, openPos, ')');
			
			if(closePos == -1) {
				throw new LuaParserException("Bad balanced brackets: " + term);
			}

			Term var = createFromString(term.substring(0, openPos), parser);
			String[] params = splitFunction(term.substring(openPos + 1, closePos), ',');
			Term[] par = new Term[0];
			if(!(params.length == 1 && params[0].trim().isEmpty())) {
				par = new Term[params.length];
				for(int i = 0; i < params.length; i++) {
					par[i] = createFromString(params[i], parser);
				}
			}
			
			return new FunctionTerm(var, par);
		}
		if(isTable(term)) {
			int openPos = term.indexOf("[");
			int closePos = findEnd(term, openPos, ']');
			
			if(closePos == -1) {
				throw new LuaParserException("Bad balanced brackings: " + term);
			}
			
			Term var = createFromString(term.substring(0, openPos), parser);
			Term index = createFromString(term.substring(openPos + 1, closePos), parser);
			
			return new TableTerm(var, index);
		}
		if(isTableDot(term)) {
			int dot = term.lastIndexOf(".");
			int end = findEndOfMember(term.substring(dot)) + dot + 1;
			
			Term var = createFromString(term.substring(0, dot), parser);
			Term index = new VarTypeTerm(new StringType(term.substring(dot + 1, end)));
			
			return new TableTerm(var, index);
		}
		if(term.matches("^\\(.*\\)$") && getTerms(term, '(', ')') == 1) {
			return createFromString(term.substring(1, term.length() - 1), parser);
		}
		if(term.contains(",")) {
			ArrayList<Term> terms = new ArrayList<Term>();
			int lastMatch = -1;
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(object.currentChar() == ',') {
					terms.add(createFromString(term.substring(lastMatch + 1, i), parser));
					lastMatch = i;
				}
			}
			
			if(lastMatch != -1) {
				terms.add(createFromString(term.substring(lastMatch + 1, term.length()), parser));
				
				return new MultiTerm(terms.toArray(new Term[terms.size()]));
			}
		}
		if(term.contains(" or ")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(term.substring(i, i + 4).equals(" or ")) {
					Term left = createFromString(term.substring(0, i), parser);
					Term right = createFromString(term.substring(i + 4), parser);
					
					return new ComputationTerm(left, right, Operator.OR);
				}
			}
		}
		if(term.contains(" and ")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(term.substring(i, i + 5).equals(" and ")) {
					Term left = createFromString(term.substring(0, i), parser);
					Term right = createFromString(term.substring(i + 5), parser);
					
					return new ComputationTerm(left, right, Operator.AND);
				}
			}
		}
		if(term.contains("<") || term.contains(">") || 
		   term.contains("<=") || term.contains(">=") ||
		   term.contains("~=") | term.contains("==")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				char c = object.currentChar();
				int next = object.peekChar();
				if(c == '>' || c == '<' ||
				   (c == '~' && next == '=') ||
				   (c == '=' && next == '=')) {
					boolean secondChar = object.peekChar() == '=';
					Term left = createFromString(term.substring(0, i), parser);
					Term right = createFromString(term.substring(i + (secondChar?2:1)), parser);
					switch(c) {
					case '>':
						return new ComputationTerm(left, right, secondChar?Operator.GREATER_EQUALS_THAN:Operator.GREATER_THAN);
					case '<':
						return new ComputationTerm(left, right, secondChar?Operator.LOWER_EQUALS_THAN:Operator.LOWER_THAN);
					case '~':
						return new ComputationTerm(left, right, Operator.NOT_EQUALS);
					case '=':
						return new ComputationTerm(left, right, Operator.EQUALS);
					}
				}
			}
		}
		if(term.contains("..")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if((object.currentChar() == '.' && object.peekChar() == '.')) {
					Term left = createFromString(term.substring(0, i), parser);
					Term right = createFromString(term.substring(i + 2), parser);
					
					return new ComputationTerm(left, right, Operator.CONCATENATE);
				}
			}
		}
		if(term.contains("+")|| term.contains("-")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				char c = object.currentChar();
				if(c == '+') {
					Term left = createFromString(term.substring(0, i), parser);
					Term right = createFromString(term.substring(i + 1, term.length()), parser);
					
					return new ComputationTerm(left, right, Operator.ADD);
				}
				if(c == '-') {
					Term left = createFromString(term.substring(0,i), parser);
					Term right = createFromString(term.substring(i + 1, term.length()), parser);
					
					return new ComputationTerm(left, right, Operator.SUBSTRACT);
				}
			}
		}
		if(term.contains("*")|| term.contains("/")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				char c = object.currentChar();
				if(c == '*') {
					Term left = createFromString(term.substring(0, i), parser);
					Term right = createFromString(term.substring(i + 1, term.length()), parser);
					
					return new ComputationTerm(left, right, Operator.MULTIPLY);
				}
				if(c == '/') {
					Term left = createFromString(term.substring(0,i), parser);
					Term right = createFromString(term.substring(i + 1, term.length()), parser);
					
					return new ComputationTerm(left, right, Operator.DIVIDE);
				}
			}
		}
		if(term.startsWith("#")) {
			return new ComputationTerm(createFromString(term.substring(1), parser), new NullTerm(), Operator.LENGTH);
		}
		if(term.matches("^not .+?")) {
			return new NotTerm(createFromString(term.substring(4), parser));
		}
		if(term.contains("^")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(object.currentChar() == '^') {
					Term left = createFromString(term.substring(0, i), parser);
					Term right = createFromString(term.substring(i + 1), parser);
					
					return new ComputationTerm(left, right, Operator.EXPONENTIATE);
				}
			}
		}
		if(term.isEmpty())
			return new NullTerm();
		throw new LuaParserException("Invalid term: \"" + term + "\": " + parser.getLine());
	}

	private static int findEndOfMember(String term) {
		SearchObject object = new SearchObject(term);
		while(!iterate(object, true).hasEnd()) {
			int i = object.getIndex();
			char c = object.currentChar();
			if(( i == term.length() || c == ')' || c == ']') && object.isTopLevel() && !object.hasQuotation()) {
				return i;
			}
		}
		
		return -1;
	}
	
	private static String[] splitFunction(String term, char d) {
		ArrayList<String> strings = new ArrayList<String>();
		
		int pos = -1;
		SearchObject object = new SearchObject(term);
		while(!iterate(object).hasEnd()) {
			int i = object.getIndex();
			char c = object.currentChar();
			
			if(c == d) {
				strings.add(term.substring(pos + 1, i));
				pos = i;
			}
		}
		
		strings.add(term.substring(pos + 1, term.length()));
		return strings.toArray(new String[strings.size()]);
	}
	
	private static int findEnd(String term, int start, char c) {
		SearchObject object = new SearchObject(term);
		while(!iterate(object, true).hasEnd()) {
			char found = object.currentChar();
			if(found == c && object.isTopLevel() && !object.hasQuotation()) {
				return object.getIndex();
			}
		}
		return -1;
	}
	
	public static boolean isFunction(String term) {
		return term.matches("^[a-zA-Z]+?[0-9a-zA-Z.]*\\s*(\\[.+?\\])?\\s*\\(.*?\\)$") && getTerms(term, '(', ')') == 1;
	}
	
	private static SearchObject iterate(SearchObject object) {
		return iterate(object, false);
	}
	
	private static SearchObject iterate(SearchObject object, boolean returnForEachChar) {
		if(!object.canRead()) {
			object.index++;
			return object;
		}
		do {
			int i = object.index;
			char c = object.nextChar();
			
			if(c == '\'' && (i == 0 || object.searchString.charAt(i - 1) != '\\')) {
				object.quotationSingle = !object.quotationSingle;
			} else if(c == '"' && (i == 0 || object.searchString.charAt(i - 1) != '\\')) {
				object.quotationDouble = !object.quotationDouble;
			} else if(!object.hasQuotation()){
				switch(c) {
				case '(':
					object.depthRound++;break;
				case '[':
					object.depthSquared++;break;
				case '{':
					object.depthCurley++;break;
				case ')':
					object.depthRound--;break;
				case ']':
					object.depthSquared--;break;
				case '}':
					object.depthCurley--;break;
				default:break;
				}
			}
		} while((!object.isTopLevel() || object.hasQuotation()) && object.canRead() && !returnForEachChar);
		
		return object;
	}
	
	private static int getTerms(String term, char open, char close) {
		char[] c = term.toCharArray();
		int depth = 0;
		boolean start = false;
		int t = 0;
		for(int i = 0; i < c.length; i++) {
			if(c[i] == open) {
				depth++;
				start = true;
			}
			if(c[i] == close)
				depth--;
			if(depth == 0 && start)
				t++;
		}
		return t;
	}
	
	public static boolean isTable(String term) {
		return term.matches("^[a-zA-Z]+?[0-9a-zA-Z]*\\s*\\[.+?\\]$") && getTerms(term, '[', ']') == 1;
	}
	
	private static boolean isTableDot(String term) {
		return term.matches("^[a-zA-Z]+?[0-9a-zA-Z]*\\s*\\.[^.].*$");
	}
	
	public static boolean isNumeric(String term) {
		try {
			Double.parseDouble(term);
			return true;
		} catch(NumberFormatException e) {
		}
		return false;
	}
	
	public static boolean isVariable(String term) {
		return term.matches("^[_a-zA-Z]+?[_0-9a-zA-Z]*$") && !isKeyword(term);
	}
	
	public static boolean isString(String term) {
		return term.matches("^[\"']([^\"']|\\\\\"|\\\\')*?[\"']$");
	}
	
	public static boolean isBoolean(String term) {
		return term.matches("^(true|false)$");
	}
	
	private static StringType toStringType(String term) {
		term = term.replace("\\n", "\n").replace("\\t", "\t").replace("\\b", "\b").replace("\\\"", "\"").replace("\\f", "\f").replace("\\'", "\'").replace("\\\\", "\\").replace("\\r", "\r");
		
		return new StringType(term.substring(1, term.length() - 1));
	}
	
	public static boolean isKeyword(String term) {
		switch(term) {
		case "and":
		case "break":
		case "do":
		case "else":
		case "end":
		case "false":
		case "for":
		case "function":
		case "if":
		case "in":
		case "local":
		case "nil":
		case "not":
		case "or":
		case "repeat":
		case "return":
		case "then":
		case "true":
		case "until":
		case "while": return true;
		default: return false;
		}
	}

	public static boolean isFunctionBegin(String term) {
		int terms = getTerms(term, '(', ')');
		return term.matches("^[a-zA-Z]+?[0-9a-zA-Z.]*\\s*(\\[.+?\\])?\\s*\\(.*") && (terms == 1 || terms == 0);
	}
	
	public static boolean isTableBegin(String term) {
		int terms = getTerms(term, '[', ']');
		return term.matches("^[a-zA-Z]+?[0-9a-zA-Z]*\\s*(\\[.+?)?$") && (terms == 1 || terms == 0);
	}
	
	private static boolean isTableDotBegin(String term) {
		return term.matches("^[a-zA-Z]+?[0-9a-zA-Z]*\\s*\\.\\s*([^.].*)?$");
	}
	

	public static boolean isValid(String term) {
		term = term.trim();
		if(isNumeric(term) || isString(term) || term.equals("nil") || isBoolean(term) || isVariable(term) || isFunctionBegin(term) || isTableBegin(term) || isTableDotBegin(term)) {
			return true;
		}
		if(term.matches("^\\{.*\\}$") && getTerms(term, '{', '}') == 1) {
			String[] content = splitFunction(term.substring(1, term.length() - 1), ',');
			for(int i = 0; i < content.length; i++) {
				String[] namepair = splitFunction(content[i], '=');
				if(namepair.length == 1 && !namepair[0].isEmpty()) {
					if(!isValid(namepair[0]))
						return false;
				} else if(namepair.length == 2) {
					if(!isValid(namepair[0]) || !isValid(namepair[1]))
						return false;
				}
			}
			return true;
		}
		if(term.matches("^\\(.*\\)$") && getTerms(term, '(', ')') == 1) {
			return isValid(term.substring(1, term.length() - 1));
		}
		if(term.contains(",")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(object.currentChar() == ',') {
					boolean left = isValid(term.substring(0, i));
					boolean right = i+1==term.length() ? true:isValid(term.substring(i + 1));
					
					return left && right;
				}
			}
		}
		if(term.contains(" or ")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(term.substring(i, i + 4).equals(" or ")) {
					boolean left = isValid(term.substring(0, i));
					boolean right = i+4==term.length() ? true:isValid(term.substring(i + 4));
					
					return left && right;
				}
			}
		}
		if(term.contains(" and ")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(term.substring(i, i + 5).equals(" and ")) {
					boolean left = isValid(term.substring(0, i));
					boolean right = i+5==term.length() ? true:isValid(term.substring(i + 5));
					
					return left && right;
				}
			}
		}
		if(term.contains("<") || term.contains(">") || 
		   term.contains("<=") || term.contains(">=") ||
		   term.contains("~=") | term.contains("==")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				char c = object.currentChar();
				int next = object.peekChar();
				int i = object.getIndex();
				if(c == '>' || c == '<' ||
				   (c == '~' && next == '=') ||
				   (c == '=' && next == '=')) {
					boolean secondChar = next == '=';
					boolean left = isValid(term.substring(0, i));
					boolean right = i+(secondChar?2:1)==term.length() ? true:isValid(term.substring(i + (secondChar?2:1)));
					
					return left && right;
				}
			}
		}
		if(term.contains("..")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if((object.currentChar() == '.' && object.peekChar() == '.')) {
					boolean left = isValid(term.substring(0, i));
					boolean right = i+2==term.length() ? true:isValid(term.substring(i + 2));
					
					return left && right;
				}
			}
		}
		if(term.contains("+")|| term.contains("-")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				char c = object.currentChar();
				int i = object.getIndex();
				if(c == '+' || c == '-') {
					boolean left = isValid(term.substring(0, i));
					boolean right = i+1==term.length() ? true:isValid(term.substring(i + 1, term.length()));
					
					return left && right;
				}
			}
		}
		if(term.contains("*")|| term.contains("/")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				char c = object.currentChar();
				int i = object.getIndex();
				if(c == '*' || c == '/') {
					boolean left = isValid(term.substring(0, i));
					boolean right = i+1==term.length() ? true:isValid(term.substring(i + 1));
					
					return left && right;
				}
			}
		}
		if(term.startsWith("#")) {
			return term.length() == 1 || isValid(term.substring(1));
		}
		if(term.matches("^not .+?")) {
			return term.length() == 4 || isValid(term.substring(4));
		}
		if(term.contains("^")) {
			SearchObject object = new SearchObject(term);
			while(!iterate(object).hasEnd()) {
				int i = object.getIndex();
				if(object.currentChar() == '^') {
					boolean left = isValid(term.substring(0, i));
					boolean right = i+1==term.length() ? true:isValid(term.substring(i + 1));
					
					return left && right;
				}
			}
		}
		
		if(term.startsWith("(")) {
			return term.length() == 1 || isValid(term.substring(1));
		}
		return false;
	}
}
