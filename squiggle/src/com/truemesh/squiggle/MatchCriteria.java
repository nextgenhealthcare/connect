package com.truemesh.squiggle;

import com.truemesh.squiggle.output.Output;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 */
public class MatchCriteria extends Criteria {

    public static final String EQUALS = "=";
    public static final String GREATER = ">";
    public static final String LESS = "<";
    public static final String GREATEREQUAL = ">=";
    public static final String LESSEQUAL = "<=";
    public static final String LIKE = "LIKE";

    private Column column;
    private String value;
    private String matchType;

    public MatchCriteria(Column column, String matchType, String value) {
        this.column = column;
        this.value = quote(value);
        this.matchType = matchType;
    }

    public MatchCriteria(Column column, String matchType, float value) {
        this.column = column;
        this.value = "" + value;
        this.matchType = matchType;
    }

    public MatchCriteria(Column column, String matchType, int value) {
        this.column = column;
        this.value = "" + value;
        this.matchType = matchType;
    }

    public MatchCriteria(Column column, String matchType, boolean value) {
        this.column = column;
        this.value = "" + value;
        this.matchType = matchType;
    }

    public MatchCriteria(Table table, String columnname, String matchType, boolean value) {
        this(table.getColumn(columnname), matchType, value);
    }

    public MatchCriteria(Table table, String columnname, String matchType, int value) {
        this(table.getColumn(columnname), matchType, value);
    }

    public MatchCriteria(Table table, String columnname, String matchType, float value) {
        this(table.getColumn(columnname), matchType, value);
    }

    public MatchCriteria(Table table, String columnname, String matchType, String value) {
        this(table.getColumn(columnname), matchType, value);
    }

    public MatchCriteria(Table table, String function, String columnname, String matchType, boolean value) {
        this(table.getColumn(function, columnname), matchType, value);
    }

    public MatchCriteria(Table table, String function, String columnname, String matchType, int value) {
        this(table.getColumn(function, columnname), matchType, value);
    }

    public MatchCriteria(Table table, String function, String columnname, String matchType, float value) {
        this(table.getColumn(function, columnname), matchType, value);
    }

    public MatchCriteria(Table table, String function, String columnname, String matchType, String value) {
        this(table.getColumn(function, columnname), matchType, value);
    }
    
    public Column getColumn() {
        return column;
    }

    public void write(Output out) {
        out.print(column)
            .print(' ')
            .print(matchType)
            .print(' ')
            .print(value);
    }

}
