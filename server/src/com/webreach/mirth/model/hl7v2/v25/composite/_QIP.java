package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _QIP extends Composite {
	public _QIP(){
		fields = new Class[]{_ST.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Segment Field Name", "Values"};
		description = "Query Input Parameter List";
		name = "QIP";
	}
}
