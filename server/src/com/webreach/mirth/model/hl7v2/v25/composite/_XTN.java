package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XTN extends Composite {
	public _XTN(){
		fields = new Class[]{_ST.class, _ID.class, _ID.class, _ST.class, _NM.class, _NM.class, _NM.class, _NM.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Telephone Number", "Telecommunication Use Code", "Telecommunication Equipment Type", "Email Address", "Country Code", "Area/City Code", "Local Number", "Extension", "Any Text", "Extension Prefix", "Speed Dial Code", "Unformatted Telephone Number"};
		description = "Extended Telecommunication Number";
		name = "XTN";
	}
}
