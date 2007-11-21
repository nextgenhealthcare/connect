package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TQ2 extends Segment {
	public _TQ2(){
		fields = new Class[]{_SI.class, _ID.class, _EI.class, _EI.class, _EI.class, _ID.class, _ID.class, _CQ.class, _NM.class, _ID.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Sequence/Results Flag", "Related Placer Number", "Related Filler Number", "Related Placer Group Number", "Sequence Condition Code", "Cyclic Entry/Exit Indicator", "Sequence Condition Time Interval", "Cyclic Group Maximum Number of Repeats", "Special Service Request Relationship"};
		description = "Timing/Quantity Relationship";
		name = "TQ2";
	}
}
