package florian.lua;

/**
 * List of all operators
 * @author Florian
 *
 */
public enum Operator {
	ADD {
		public String toString() {
			return "+";
		}
	},
	SUBSTRACT {
		public String toString() {
			return "-";
		}
	},
	MULTIPLY {
		public String toString() {
			return "*";
		}
	},
	DIVIDE {
		public String toString() {
			return "/";
		}
	},
	EQUALS {
		public String toString() {
			return "==";
		}
	},
	NOT_EQUALS {
		public String toString() {
			return "!=";
		}
	},
	LOWER_THAN {
		public String toString() {
			return "<";
		}
	},
	GREATER_THAN {
		public String toString() {
			return ">";
		}
	},
	LOWER_EQUALS_THAN {
		public String toString() {
			return "<=";
		}
	},
	GREATER_EQUALS_THAN {
		public String toString() {
			return ">=";
		}
	},
	CONCATENATE  {
		public String toString() {
			return "..";
		}
	},
	EXPONENTIATE {
		public String toString() {
			return "^";
		}
	},
	LENGTH {
		public String toString() {
			return "#";
		}
	},
	OR {
		public String toString() {
			return "or";
		}
	},
	AND {
		public String toString() {
			return "and";
		}
	}
}
