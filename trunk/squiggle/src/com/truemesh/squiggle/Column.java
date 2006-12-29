package com.truemesh.squiggle;

import com.truemesh.squiggle.output.Outputable;
import com.truemesh.squiggle.output.Output;
import com.truemesh.squiggle.output.ToStringer;

/**
 * @author <a href="joe@truemesh.com">Joe Walnes</a>
 */
public class Column implements Outputable {

	private String name;
	private Table table;
	private String function;

	public Column(Table table, String name) {
		this.table = table;
		this.name = name;
	}

	public Column(Table table, String function, String name) {
		this.table = table;
		this.function = function;
		this.name = name;
	}

	public Table getTable() {
		return table;
	}

	public String getName() {
		return name;
	}

	public String getFunction() {
		return this.function;
	}

	public String toString() {
		return ToStringer.toString(this);
	}

	public void write(Output out) {
		StringBuilder builder = new StringBuilder();

		if (function != null) {
			builder.append(getFunction() + "(");
		}

		builder.append(getTable().getAlias());
		builder.append(".");
		builder.append(getName());
		
		if (getFunction() != null) {
			builder.append(")");
		}

		out.print(builder.toString());
	}

}
