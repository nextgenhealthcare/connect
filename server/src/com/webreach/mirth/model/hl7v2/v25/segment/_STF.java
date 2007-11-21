package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _STF extends Segment {
	public _STF(){
		fields = new Class[]{_CE.class, _CX.class, _XPN.class, _IS.class, _IS.class, _TS.class, _ID.class, _CE.class, _CE.class, _XTN.class, _XAD.class, _DIN.class, _DIN.class, _CE.class, _ST.class, _CE.class, _CE.class, _ST.class, _JCC.class, _CE.class, _ID.class, _DLN.class, _ID.class, _DT.class, _DT.class, _DT.class, _CE.class, _CE.class, _ID.class, _CE.class, _TS.class, _ID.class, _CWE.class, _DR.class, _DT.class, _CWE.class, _ID.class, _CWE.class};
		repeats = new int[]{0, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, -1, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Staff Identifier List", "Staff Name", "Staff Type", "Administrative Sex", "Date/Time of Birth", "Active/Inactive Flag", "Department", "Hospital Service", "Phone", "Office/Home Address/Birthplace", "Institution Activation Date", "Institution Inactivation Date", "Backup Person ID", "E-mail Address", "Preferred Method of Contact", "Marital Status", "Job Title", "Job Code/Class", "Employment Status Code", "Additional Insured On Auto", "Driver's License Number - Staff", "Copy Auto Ins", "Auto Ins. Expires", "Date Last Dmv Review", "Date Next Dmv Review", "Race", "Ethnic Group", "Re-activation Approval Indicator", "Citizenship", "Death Date and Time", "Death Indicator", "Institution Relationship Type Code", "Institution Relationship Period", "Expected Return Date", "Cost Center Code", "Generic Classification Indicator", "Inactive Reason Code"};
		description = "Staff Identification";
		name = "STF";
	}
}
