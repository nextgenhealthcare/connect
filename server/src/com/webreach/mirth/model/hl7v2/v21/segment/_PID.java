package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PID extends Segment {
	public _PID(){
		fields = new Class[]{_SI.class, _CK.class, _CK.class, _ST.class, _PN.class, _ST.class, _DT.class, _ID.class, _PN.class, _ID.class, _AD.class, _ID.class, _TN.class, _TN.class, _ST.class, _ID.class, _ID.class, _CK.class, _ST.class, _CM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Patient ID (External Id)", "Patient ID (Internal Id)", "Alternate Patient ID", "Patient's Name", "Mother's Maiden Name", "Date of Birth", "Sex", "Patient Alias", "Ethnic Group", "Patient Address", "County Code", "Phone Number - Home", "Phone Number - Business", "Language - Patient", "Marital Status", "Religion", "Patient Account Number", "Ssn Number - Patient", "Drivers License - Patient"};
		description = "Patient Identification";
		name = "PID";
	}
}
