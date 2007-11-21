package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _FN extends Composite {
	public _FN(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Surname", "Own Surname Prefix", "Own Surname", "Surname Prefix From Partner/Spouse", "Surname From Partner/Spouse"};
		description = "Family Name";
		name = "FN";
	}
}
