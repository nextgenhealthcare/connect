package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _EIP extends Composite {
	public _EIP(){
		fields = new Class[]{_EI.class, _EI.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Placer Assigned Identifier", "Filler Assigned Identifier"};
		description = "Entity Identifier Pair";
		name = "EIP";
	}
}
