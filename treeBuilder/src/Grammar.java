import java.util.LinkedList;
import java.util.Arrays;

/**
* Conveniently stores the data related to a grammar
*/
public class Grammar {
    private LinkedList<GrammarNode> nodes;
    
    /**
    * Default Constructor
    */
    public Grammar() {
        this.nodes = new LinkedList<GrammarNode>();
    }
    
    /**
    * Adds a new, empty rule to the grammar
    * @param ruleName - the name of the rule
    * @return the index at which the added rule resides
    */
    public int addNode(String ruleName) {
        nodes.addLast(new GrammarNode(ruleName));
        return (nodes.size() - 1);
    }
    
    /**
    * Adds a new rule to the grammar with a single expression
    * @param ruleName - the name of the rule
    * @param expression - the expression
    * @return the index at which the added rule resides
    */
    public int addNode(String ruleName, String expression) {
        nodes.addLast(new GrammarNode(ruleName, expression));
        return (nodes.size() - 1);
    }
    
    /**
    * Adds a new rule to the grammar with a set of expressions
    * @param ruleName - the name of the rule
    * @param expressions - the set of expressions
    * @return the index at which the added rule resides
    */
    public int addNode(String ruleName, String expressions[]) {
        nodes.addLast(new GrammarNode(ruleName, expressions));
        return (nodes.size() - 1);
    }
    
    /**
    * Adds an expression to a specific rule in the grammar
    * @param index - the index of the rule to add to
    * @param s - expression to add
    */
    public void addExpressionToNode(int index, String s) {
        nodes.get(index).addExpression(s);
    }
    
    /**
    * Adds a set of expressions to a specific rule in the grammar
    * @param index - the index of the rule to add to
    * @param s - expressions to add
    */
    public void addExpressionToNode(int index, String s[]) {
        nodes.get(index).addExpression(s);
    }
    
    @Override
    public String toString() {
        String str = "";
        
        for (GrammarNode s : nodes) {
            str += s.toString() + "\n";
        }
        
        return str;
    }
    
    /**
    * Stores data about a single rule in a grammar
    */
    private class GrammarNode {
        private String ruleName;
        private LinkedList<String> expressions = new LinkedList<String>();
        
        /**
        * Constructs an empty rule
        * @param ruleName - the name of this rule
        */
        public GrammarNode(String ruleName) {
            this.ruleName = ruleName;
        }
        
        /**
        * Constructs a rule with a single expression
        * @param ruleName - the name of this rule
        * @param expression - the expression to add
        */
        public GrammarNode(String ruleName, String expression) {
            this.ruleName = ruleName;
            this.expressions.add(expression);
        }
        
        /**
        * Constructs a rule with a set of expressions
        * @param ruleName - the name of this rule
        * @param expressions - the expressions to add
        */
        public GrammarNode(String ruleName, String expressions[]) {
            this.ruleName = ruleName;
            this.expressions.addAll(Arrays.asList(expressions));
        }
        
        /**
        * Add a single expression to the rule
        * @param s - the expression to add
        */
        public void addExpression(String s) {
            this.expressions.addLast(s);
        }
        
        /**
        * Adds a set of expressions to the rule
        * @param s - an array containing the expressions to add
        */
        public void addExpression(String s[]) {
            this.expressions.addAll(Arrays.asList(s));
        }
        
        @Override
        public String toString() {
            String str = "";
            
            str += "<" + ruleName + ">";
            str += " ::= ";
            for (int i = 0; i < expressions.size(); i++) {
                if (i != 0) {
                    str += " | ";
                }
                str += expressions.get(i);
            }
            
            return str;
        }
    }
}