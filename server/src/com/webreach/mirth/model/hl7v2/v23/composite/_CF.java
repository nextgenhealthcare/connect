package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CF extends Composite {
	public _CF(){
		fields = new Class[]{_ID.class, _FT.class, _ST.class, _ID.class, _FT.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Identifier", "Formatted Text", "Name of Coding System", "Alternate Identifier", "Alternate Formatted Text", "Name of Alternate Coding System"};
		description = "Coded Element with Formatted Values";
		name = "CF";
	}
}
