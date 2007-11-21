package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _MO extends Composite {
	public _MO(){
		fields = new Class[]{_NM.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Quantity", "Denomination"};
		description = "Money";
		name = "MO";
	}
}
