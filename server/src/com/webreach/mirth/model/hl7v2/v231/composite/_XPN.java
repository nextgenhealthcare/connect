package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XPN extends Composite {
	public _XPN(){
		fields = new Class[]{_FN.class, _ST.class, _ST.class, _ST.class, _ST.class, _IS.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Family Last Name", "Given Name", "Middle Initial or Name", "Suffix", "Prefix", "Degree", "Name Type Code", "Name Representation Code"};
		description = "Extended Person Name";
		name = "XPN";
	}
}
