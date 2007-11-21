package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DTN extends Composite {
	public _DTN(){
		fields = new Class[]{_IS.class, _NM.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Day Type", "Number of Days"};
		description = "Day Type and Number";
		name = "DTN";
	}
}
