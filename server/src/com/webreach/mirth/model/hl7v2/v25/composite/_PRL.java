package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PRL extends Composite {
	public _PRL(){
		fields = new Class[]{_CE.class, _ST.class, _TX.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Parent Observation Identifier", "Parent Observation Sub-identifier", "Parent Observation Value Descriptor"};
		description = "Parent Result Link";
		name = "PRL";
	}
}
