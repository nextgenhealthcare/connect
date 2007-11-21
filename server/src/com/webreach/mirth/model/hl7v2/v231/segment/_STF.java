package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _STF extends Segment {
	public _STF(){
		fields = new Class[]{_CE.class, _CX.class, _XPN.class, _IS.class, _IS.class, _TS.class, _ID.class, _CE.class, _CE.class, _XTN.class, _XAD.class, _DIN.class, _DIN.class, _CE.class, _ST.class, _CE.class, _CE.class, _ST.class, _JCC.class, _IS.class, _ID.class, _DLN.class, _ID.class, _DT.class, _DT.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Staff ID Code", "Staff Name", "Staff Type", "Sex", "Date/Time of Birth", "Active/Inactive Flag", "Department", "Hospital Service", "Phone", "Office/Home Address", "Institution Activation Date", "Institution Inactivation Date", "Backup Person ID", "E-mail Address", "Preferred Method of Contact", "Marital Status", "Job Title", "Job Code/Class", "Employment Status", "Additional Insured On Auto", "Driver’s License Number - Staff", "Copy Auto Ins", "Auto Ins. Expires", "Date Last Dmv Review", "Date Next Dmv Review"};
		description = "Staff Identification";
		name = "STF";
	}
}
