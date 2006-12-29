package com.truemesh.squiggle;

import com.truemesh.squiggle.output.Output;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 */
public class InCriteria extends Criteria {

    private Column column;
    private String value;
    private SelectQuery subSelect;

    public InCriteria(Column column, Collection values) {
        this.column = column;
        StringBuffer v = new StringBuffer();
        Iterator i = values.iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            Object curr = i.next();
            hasNext = i.hasNext();
            if (curr instanceof Number) {
                v.append(curr);
            } else {
                v.append(quote(curr.toString()));
            }
            if (hasNext) v.append(',');
        }
        this.value = v.toString();
    }

    public InCriteria(Column column, String[] values) {
        this.column = column;
        StringBuffer v = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            v.append(quote(values[i]));
            if (i < values.length - 1) v.append(',');
        }
        this.value = v.toString();
    }

    public InCriteria(Column column, int[] values) {
        this.column = column;
        StringBuffer v = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            v.append(values[i]);
            if (i < values.length - 1) v.append(',');
        }
        this.value = v.toString();
    }

    public InCriteria(Column column, float[] values) {
        this.column = column;
        StringBuffer v = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            v.append(values[i]);
            if (i < values.length - 1) v.append(',');
        }
        this.value = v.toString();
    }

    public InCriteria(Column column, SelectQuery subSelect) {
        this.column = column;
        this.subSelect = subSelect;
    }

    public InCriteria(Column column, String subSelect) {
        this.column = column;
        this.value = subSelect;
    }

    public InCriteria(Table table, String columnname, Collection values) {
        this(table.getColumn(columnname), values);
    }

    public InCriteria(Table table, String columnname, float[] values) {
        this(table.getColumn(columnname), values);
    }

    public InCriteria(Table table, String columnname, int[] values) {
        this(table.getColumn(columnname), values);
    }

    public InCriteria(Table table, String columnname, SelectQuery subSelect) {
        this(table.getColumn(columnname), subSelect);
    }

    public InCriteria(Table table, String columnname, String subSelect) {
        this(table.getColumn(columnname), subSelect);
    }

    public InCriteria(Table table, String columnname, String[] values) {
        this(table.getColumn(columnname), values);
    }

    public Column getColumn() {
        return column;
    }

    public void write(Output out) {
        out.print(column);
        out.println(" IN (");

        out.indent();
        if (subSelect != null) {
            subSelect.write(out);
        } else {
            out.print(value);
        }
        out.unindent();

        out.println();
        out.print(")");
    }

}
