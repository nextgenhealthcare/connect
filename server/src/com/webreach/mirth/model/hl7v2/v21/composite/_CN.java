package com.webreach.mirth.model.hl7v2.v21.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CN extends Composite {
	public _CN(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID Number", "Family Name", "Given Name", "Middle Initial", "Suffix", "Prefix"};
		description = "Composite ID Number and Name";
		name = "CN";
	}
}
