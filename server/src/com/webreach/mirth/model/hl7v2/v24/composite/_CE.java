package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CE extends Composite {
	public _CE(){
		fields = new Class[]{_ST.class, _ST.class, _IS.class, _ST.class, _ST.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Identifier (ST)", "Text", "Name of Coding System", "Alternate Identifier (ST)", "Alternate Text", "Name of Alternate Coding System"};
		description = "Coded Element";
		name = "CE";
	}
}
