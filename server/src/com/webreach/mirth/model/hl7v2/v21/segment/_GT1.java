package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _GT1 extends Segment {
	public _GT1(){
		fields = new Class[]{_SI.class, _ID.class, _PN.class, _PN.class, _AD.class, _TN.class, _TN.class, _DT.class, _ID.class, _ID.class, _ID.class, _ST.class, _DT.class, _DT.class, _NM.class, _ST.class, _AD.class, _TN.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Guarantor Number", "Guarantor Name", "Guarantor Spouse Name", "Guarantor Address", "Guarantor Phone - Home", "Guarantor Phone - Business", "Guarantor Date of Birth", "Guarantor Sex", "Guarantor Type", "Guarantor Relationship", "Guarantor Ssn", "Guarantor Date - Begin", "Guarantor Date - End", "Guarantor Priority", "Guarantor Employer Name", "Guarantor Employer Addr", "Guarantor Employer Phone", "Guarantor Employee ID #", "Guarantor Employmt Status"};
		description = "Guarantor";
		name = "GT1";
	}
}
