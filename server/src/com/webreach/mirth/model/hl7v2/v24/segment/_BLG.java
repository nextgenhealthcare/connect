package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BLG extends Segment {
	public _BLG(){
		fields = new Class[]{_CCD.class, _ID.class, _CX.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"When to Charge", "Charge Type", "Account ID"};
		description = "Billing";
		name = "BLG";
	}
}
