package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PID extends Segment {
	public _PID(){
		fields = new Class[]{_SI.class, _CK.class, _CK.class, _ST.class, _PN.class, _ST.class, _TS.class, _ID.class, _PN.class, _ID.class, _AD.class, _ID.class, _TN.class, _TN.class, _ST.class, _ID.class, _ID.class, _CK.class, _ST.class, _CM.class, _CK.class, _ID.class};
		repeats = new int[]{0, 0, -1, 0, 0, 0, 0, 0, -1, 0, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Patient ID (External ID)", "Patient ID (Internal ID)", "Alternate Patient ID", "Patient Name", "Mother's Maiden Name", "Date of Birth", "Sex", "Patient Alias", "Race", "Patient Address", "County Code", "Phone Number - Home", "Phone Number - Business", "Language - Patient", "Marital Status", "Religion", "Patient Account Number", "SSN Number - Patient", "Driver's Lic Num - Patient", "Mother's Identifier", "Ethnic Group"};
		description = "Patient Identification";
		name = "PID";
	}
}
