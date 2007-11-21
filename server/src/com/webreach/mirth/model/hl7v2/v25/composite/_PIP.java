package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PIP extends Composite {
	public _PIP(){
		fields = new Class[]{_CE.class, _CE.class, _DT.class, _DT.class, _EI.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Privilege", "Privilege Class", "Expiration Date", "Activation Date", "Facility"};
		description = "Practitioner Institutional Privileges";
		name = "PIP";
	}
}
