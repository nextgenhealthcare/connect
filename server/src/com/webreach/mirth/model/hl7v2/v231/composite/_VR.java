package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _VR extends Composite {
	public _VR(){
		fields = new Class[]{_ST.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"First Data Code Value", "Last Data Code Calue"};
		description = "Value Qualifier";
		name = "VR";
	}
}
