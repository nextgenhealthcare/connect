package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BLG extends Segment {
	public _BLG(){
		fields = new Class[]{_CM.class, _ID.class, _CK.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"When to Charge", "Charge Type", "Account ID"};
		description = "Billing";
		name = "BLG";
	}
}
