package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _FN extends Composite {
	public _FN(){
		fields = new Class[]{_ST.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Family Name", "Last Name Prefix"};
		description = "Family + Last Name Prefix";
		name = "FN";
	}
}
