package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _TQ extends Composite {
	public _TQ(){
		fields = new Class[]{_CQ.class, _ST.class, _ST.class, _TS.class, _TS.class, _ID.class, _ST.class, _TX.class, _ID.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Quantity", "Interval", "Duration", "Start Date/Time", "End Date/Time", "Priority", "Condition", "Text", "Conjunction", "Order Sequencing"};
		description = "Timing/Quantity";
		name = "TQ";
	}
}
