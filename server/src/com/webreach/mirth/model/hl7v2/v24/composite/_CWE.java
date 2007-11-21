package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CWE extends Composite {
	public _CWE(){
		fields = new Class[]{_ST.class, _ST.class, _IS.class, _ST.class, _ST.class, _IS.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Identifier (ST)", "Text", "Name of Coding System", "Alternate Identifier (ST)", "Alternate Text", "Name of Alternate Coding System", "Coding System Version ID", "Alternate Coding System Version ID", "Original Text"};
		description = "Coded with Exceptions";
		name = "CWE";
	}
}
