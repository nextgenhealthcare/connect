package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TQ1 extends Segment {
	public _TQ1(){
		fields = new Class[]{_SI.class, _CQ.class, _RPT.class, _TM.class, _CQ.class, _CQ.class, _TS.class, _TS.class, _CWE.class, _TX.class, _TX.class, _ID.class, _CQ.class, _NM.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, 0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Quantity", "Repeat Pattern", "Explicit Time", "Relative Time and Units", "Service Duration", "Start Date/Time", "End Date/Time", "Priority", "Condition Text", "Text Instruction", "Conjunction", "Occurrence Duration", "Total Occurrence's"};
		description = "Timing/Quantity";
		name = "TQ1";
	}
}
