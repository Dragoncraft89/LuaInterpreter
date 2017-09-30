package florian.lua.parser;

public class SearchObject {
	public int index;
	public String searchString;
	public boolean quotationSingle;
	public boolean quotationDouble;
	public int depthRound;
	public int depthSquared;
	public int depthCurley;
	
	public SearchObject(String searchString) {
		this.searchString = searchString;
	}
	
	public int getIndex() {
		return index - 1;
	}
	
	public boolean isTopLevel() {
		return depthRound == 0 && depthSquared == 0 && depthCurley == 0;
	}
	
	public boolean hasQuotation() {
		return quotationSingle || quotationDouble;
	}

	public char currentChar() {
		return searchString.charAt(index-1);
	}

	public char nextChar() {
		return searchString.charAt(index++);
	}

	public int peekChar() {
		if(index<searchString.length()) {
			return searchString.charAt(index);
		}
		
		return -1;
	}

	public boolean hasEnd() {
		return index > searchString.length();
	}
	
	public boolean canRead() {
		return index < searchString.length();
	}

}
