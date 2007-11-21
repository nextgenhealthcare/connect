package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PN extends Composite {
	public _PN(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Family Name", "Given Name", "Middle Initial or Name", "Suffix", "Prefix", "Degree"};
		description = "Person Name";
		name = "PN";
	}
}
