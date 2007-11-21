package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PRL extends Composite {
	public _PRL(){
		fields = new Class[]{_CE.class, _ST.class, _TX.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"OBX-3 Observation Identifier of Parent Result", "OBX-4 Sub-id of Parent Result", "Part of OBX-5 Observation Result From Parent"};
		description = "Parent Result Link";
		name = "PRL";
	}
}
