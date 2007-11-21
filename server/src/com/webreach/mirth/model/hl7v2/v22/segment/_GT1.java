package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _GT1 extends Segment {
	public _GT1(){
		fields = new Class[]{_SI.class, _ID.class, _PN.class, _PN.class, _AD.class, _TN.class, _TN.class, _DT.class, _ID.class, _ID.class, _ID.class, _ST.class, _DT.class, _DT.class, _NM.class, _ST.class, _AD.class, _TN.class, _ST.class, _ID.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID Guarantor", "Guarantor Number", "Guarantor Name", "Guarantor Spouse Name", "Guarantor Address", "Guarantor Ph Num Home", "Guarantor Ph Num Business", "Guarantor Date of Birth", "Guarantor Sex", "Guarantor Type", "Guarantor Relationship", "Guarantor Ssn", "Guarantor Date - Begin", "Guarantor Date - End", "Guarantor Priority", "Guarantor Employer Name", "Guarantor Employer Address", "Guarantor Employ Phone Number", "Guarantor Employee ID Num", "Guarantor Employment Status", "Guarantor Organization"};
		description = "Guarantor";
		name = "GT1";
	}
}
