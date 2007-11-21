package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XPN extends Composite {
	public _XPN(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Family Name", "Given Name", "Middle Initial or Name", "Suffix", "Prefix", "Degree", "Name Type Code"};
		description = "Extended Person Name";
		name = "XPN";
	}
}
