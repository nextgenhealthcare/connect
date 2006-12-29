package com.truemesh.squiggle;



/**
 * Used for adding CRITERIA1 AND CRITERIA2 to a statement.
 * <p/>
 * <pre>
 * SelectQuery select = ...
 * ...
 * Criteria a = new MatchCriteria(table, col1, "=", 1);
 * Criteria b = new MatchCriteria(table, col2, "=", 2);
 * select.addCriteria(new AND(a, b));
 * // ( table.col1 = 1 AND table.col2 = 2 )
 * </pre>
 * 
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 */
public class AND extends BaseLogicGroup {

    public AND(Criteria left, Criteria right) {
        super("AND", left, right);
    }

}
