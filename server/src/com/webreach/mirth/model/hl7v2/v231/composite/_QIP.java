package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _QIP extends Composite {
	public _QIP(){
		fields = new Class[]{_ST.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Field Name", "Value"};
		description = "Query Input Parameter List";
		name = "QIP";
	}
}
