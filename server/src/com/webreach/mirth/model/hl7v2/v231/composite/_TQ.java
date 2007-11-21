package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _TQ extends Composite {
	public _TQ(){
		fields = new Class[]{_CQ.class, _RI.class, _ST.class, _TS.class, _TS.class, _ST.class, _ST.class, _ST.class, _ST.class, _OSD.class, _CE.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Quantity", "Interval", "Duration", "Start Date/Time", "End Date/Time", "Priority", "Condition", "Text", "Conjunction", "Order Sequencing", "Occurrence Duration", "Total Occurences"};
		description = "Timing Quantity";
		name = "TQ";
	}
}
