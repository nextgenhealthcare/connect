package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DRG extends Segment {
	public _DRG(){
		fields = new Class[]{_CE.class, _TS.class, _ID.class, _IS.class, _CE.class, _NM.class, _CP.class, _IS.class, _CP.class, _ID.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Diagnostic Related Group", "Drg Assigned Date/Time", "Drg Approval Indicator", "Drg Grouper Review Code", "Outlier Type", "Outlier Days", "Outlier Cost", "Drg Payor", "Outlier Reimbursement", "Confidential Indicator", "Drg Transfer Type"};
		description = "Diagnosis Related Group";
		name = "DRG";
	}
}
