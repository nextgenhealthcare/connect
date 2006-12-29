package com.truemesh.squiggle;

/**
 * Special column to represent For SELECT * FROM ...
 * 
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 */
public class WildCardColumn extends Column {

    public WildCardColumn(Table table) {
        super(table, "*");
    }

}
