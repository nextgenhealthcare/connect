package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _DIN extends Composite {
	public _DIN(){
		fields = new Class[]{_TS.class, _CE.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Date", "Institution Name"};
		description = "Activation Date";
		name = "DIN";
	}
}
