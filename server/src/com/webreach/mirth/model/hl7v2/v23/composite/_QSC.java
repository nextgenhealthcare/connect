package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _QSC extends Composite {
	public _QSC(){
		fields = new Class[]{_ST.class, _ID.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Name of Field", "Relational Operator", "Value", "Relational Conjunction"};
		description = "Query Selection Criteria";
		name = "QSC";
	}
}
