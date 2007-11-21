package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _EIP extends Composite {
	public _EIP(){
		fields = new Class[]{_EI.class, _EI.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Parent´s Placer Order Number", "Parent´s Filler Order Number"};
		description = "Parent Order";
		name = "EIP";
	}
}
