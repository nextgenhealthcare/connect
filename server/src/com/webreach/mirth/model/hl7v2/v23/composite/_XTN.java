package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XTN extends Composite {
	public _XTN(){
		fields = new Class[]{_ST.class, _ID.class, _ID.class, _ST.class, _NM.class, _NM.class, _NM.class, _NM.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Number", "Telecommunications Use Code", "Telecommunications Equipment Type", "Email Address", "Country Code", "Area/City Code", "Phone Number", "Extension", "Any Text"};
		description = "Extended Telecommunications Number";
		name = "XTN";
	}
}
