package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PI extends Composite {
	public _PI(){
		fields = new Class[]{_ST.class, _IS.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"ID Number (ST)", "Type of ID Number (IS)", "Other Qualifying Info"};
		description = "Person Identifier";
		name = "PI";
	}
}
