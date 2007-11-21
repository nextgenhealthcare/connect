package com.webreach.mirth.model.hl7v2.v22.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CE extends Composite {
	public _CE(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Identifier", "Text", "Name of Coding System"};
		description = "Coded Element";
		name = "CE";
	}
}
