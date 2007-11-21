package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DRG extends Segment {
	public _DRG(){
		fields = new Class[]{_CE.class, _TS.class, _ID.class, _IS.class, _CE.class, _NM.class, _CP.class, _IS.class, _CP.class, _ID.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Diagnostic Related Group", "DRG Assigned Date/Time", "DRG Approval Indicator", "DRG Grouper Review Code", "Outlier Type", "Outlier Days", "Outlier Cost", "DRG Payor", "Outlier Reimbursement", "Confidential Indicator", "DRG Transfer Type"};
		description = "Diagnosis Related Group";
		name = "DRG";
	}
}
