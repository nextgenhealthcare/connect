package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PID extends Segment {
	public _PID(){
		fields = new Class[]{_SI.class, _CX.class, _CX.class, _CX.class, _XPN.class, _XPN.class, _TS.class, _IS.class, _XPN.class, _IS.class, _XAD.class, _IS.class, _XTN.class, _XTN.class, _CE.class, _IS.class, _IS.class, _CX.class, _ST.class, _DLN.class, _CX.class, _IS.class, _ST.class, _ID.class, _NM.class, _IS.class, _CE.class, _CE.class, _TS.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Patient ID (External ID)", "Patient ID (Internal ID)", "Alternate Patient ID", "Patient Name", "Mother's Maiden Name", "Date/Time of Birth", "Sex", "Patient Alias", "Race", "Patient Address", "County Code", "Phone Number - Home", "Phone Number - Business", "Primary Language", "Marital Status", "Religion", "Patient Account Number", "SSN Number - Patient", "Driver's Licence Number-Patient", "Mother's Identifier", "Ethnic Group", "Birth Place", "Multiple Birth Indicator", "Birth Order", "Citizenship", "Veterans Military Status", "Nationalty", "Patient Death Date and Time", "Patient Death Indicator"};
		description = "Patient Identification";
		name = "PID";
	}
}
