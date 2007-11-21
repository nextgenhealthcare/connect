package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _EI extends Composite {
	public _EI(){
		fields = new Class[]{_ST.class, _IS.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Entity Identifier", "Namespace ID", "Universal ID", "Universal ID Type"};
		description = "Entity Identifier";
		name = "EI";
	}
}
