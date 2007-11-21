package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _UB2 extends Segment {
	public _UB2(){
		fields = new Class[]{_SI.class, _ST.class, _ID.class, _ST.class, _ST.class, _CM.class, _CM.class, _CM.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 7, 0, 0, 12, 8, 2, 2, 2, 0, 3, 23, 5, 0, 2};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Co-insurance Days (9)", "Condition Code (24-30)", "Covered Days (7)", "Non-covered Days (8)", "Value Amount & Code", "Occurrence Code & Date (32-35)", "Occurrence Span Code/Dates (36)", "UB92 Locator 2 (state)", "UB92 Locator 11 (state)", "UB92 Locator 31 (national)", "Document control number", "UB92 Locator 49 (national)", "UB92 Locator 56 (state)", "UB92 Locator 57 (national)", "UB92 Locator 78 (state)"};
		description = "UB92 Data";
		name = "UB2";
	}
}
