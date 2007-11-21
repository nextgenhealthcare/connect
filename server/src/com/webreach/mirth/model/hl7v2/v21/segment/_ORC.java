package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORC extends Segment {
	public _ORC(){
		fields = new Class[]{_ST.class, _CM.class, _CM.class, _CM.class, _ST.class, _ST.class, _CM.class, _CM.class, _TS.class, _CN.class, _CN.class, _CN.class, _CM.class, _TN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Order Control", "Placer Order #", "Filler Order #", "Placer Order #", "Order Status", "Response Flag", "Timing/Quantity", "Parent", "Date/Time of Transaction", "Entered By", "Verified By", "Ordering Provider", "Enterer's Location", "Call Back Phone Number"};
		description = "Common Order";
		name = "ORC";
	}
}
