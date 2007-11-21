package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCH extends Segment {
	public _PCH(){
		fields = new Class[]{_XON.class, _CX.class, _IS.class, _ST.class, _ST.class, _ST.class, _DT.class, _DT.class, _ST.class, _XAD.class, _XTN.class, _XON.class, _IS.class, _ST.class, _IS.class, _PPN.class, _ST.class, _XPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Hospital", "External ID", "Privilege", "Is Restricted", "Department", "Is Primary", "Appointment Date", "Reappointment Date", "Is Outside Service", "Address", "Phone", "Admitter", "Specialty", "Comment", "Standing", "Revised By", "Percent Admit", "Contact Person"};
		description = "Hospital Information Segment";
		name = "PCH";
	}
}
