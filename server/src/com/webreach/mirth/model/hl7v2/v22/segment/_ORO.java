package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORO extends Segment {
	public _ORO(){
		fields = new Class[]{_CE.class, _ID.class, _CN.class, _ID.class};
		repeats = new int[]{0, 0, -1, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Order Item ID", "Substitute Allowed", "Results Copied To", "Stock Location"};
		description = "Order";
		name = "ORO";
	}
}
