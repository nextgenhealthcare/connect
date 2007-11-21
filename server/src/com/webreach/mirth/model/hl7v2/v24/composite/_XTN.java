package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XTN extends Composite {
	public _XTN(){
		fields = new Class[]{_TN.class, _ID.class, _ID.class, _ST.class, _NM.class, _NM.class, _NM.class, _NM.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"[(999)] 999-9999 [x99999][c Any Text]", "Telecommunication Use Code", "Telecommunication Equipment Type (ID)", "Email Address", "Country Code", "Area/City Code", "Phone Number", "Extension", "Any Text"};
		description = "Extended Telecommunication Number";
		name = "XTN";
	}
}
