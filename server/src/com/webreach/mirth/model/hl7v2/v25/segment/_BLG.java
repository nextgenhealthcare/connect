package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BLG extends Segment {
	public _BLG(){
		fields = new Class[]{_CCD.class, _ID.class, _CX.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"When to Charge", "Charge Type", "Account ID", "Charge Type Reason"};
		description = "Billing";
		name = "BLG";
	}
}
