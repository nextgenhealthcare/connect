package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NK1 extends Segment {
	public _NK1(){
		fields = new Class[]{_SI.class, _XPN.class, _CE.class, _XAD.class, _XTN.class, _XTN.class, _CE.class, _DT.class, _DT.class, _ST.class, _JCC.class, _CX.class, _XON.class, _IS.class, _IS.class, _TS.class, _IS.class, _IS.class, _IS.class, _CE.class, _IS.class, _CE.class, _ID.class, _IS.class, _IS.class, _XPN.class, _CE.class, _IS.class, _CE.class, _XPN.class, _XTN.class, _XAD.class, _CX.class, _IS.class, _IS.class, _IS.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Name", "Relationship", "Address", "Phone Number", "Business Phone Number", "Contact Role", "Start Date", "End Date", "Next of Kin/Associated Parties Job Title", "Next of Kin/Associated Parties Job Code/Class", "Next of Kin/Associated Parties Employee Number", "Organization Name", "Marital Status", "Sex", "Date/Time of Birth", "Living Dependency", "Ambulatory Status", "Citizenship", "Primary Language", "Living Arrangement", "Publicity Indicator", "Protection Indicator", "Student Indicator", "Religion", "Mother’s Maiden Name", "Nationality", "Ethnic Group", "Contact Reason", "Contact Person’s Name", "Contact Person’s Telephone Number", "Contact Person’s Address", "Next of Kin/Associated Party’s Identifiers", "Job Status", "Race", "Handicap", "Contact Person Social Security Number"};
		description = "Next of Kin/Associated Parties";
		name = "NK1";
	}
}
