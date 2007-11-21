package com.webreach.mirth.model.hl7v2.v22.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PN extends Composite {
	public _PN(){
		fields = new Class[]{_String.class, _String.class, _String.class, _String.class, _String.class, _String.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Family Name", "Given Name", "Middle Initial", "Suffix", "Prefix", "Degree"};
		description = "Person Name";
		name = "PN";
	}
}
