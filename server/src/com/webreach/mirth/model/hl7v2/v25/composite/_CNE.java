package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CNE extends Composite {
	public _CNE(){
		fields = new Class[]{_ST.class, _ST.class, _ID.class, _ST.class, _ST.class, _ID.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Identifier", "Text", "Name of Coding System", "Alternate Identifier", "Alternate Text", "Name of Alternate Coding System", "Coding System Version ID", "Alternate Coding System Version ID", "Original Text"};
		description = "Coded with No Exceptions";
		name = "CNE";
	}
}
