package com.webreach.mirth.model.hl7v2.v22.segment;
import com.webreach.mirth.model.hl7v2.v22.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _UB1 extends Segment {
	public _UB1(){
		fields = new Class[]{_SI.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class, _ST.class, _ST.class, _CM.class, _NM.class, _ID.class, _ID.class, _DT.class, _DT.class, _CM.class, _ID.class, _DT.class, _DT.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 5, 0, 0, 8, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Blood Deductible", "Blood Furnished-pints of (40)", "Blood Replaced-pints (41)", "Blood not Replaced-pints(42)", "Co-insurance days (25)", "Condition code", "Covered days - (23)", "Non Covered Days - (24)", "Value Amount & Code", "Number of Grace Days (90)", "Spec prog indicator (44)", "PSRO/UR Approval ind (87)", "PSRO/UR Approved Stay-From (88)", "PSRO/UR Approved Stay-To (89)", "Occurrence (28-32)", "Occurrence Span (33)", "Occurrence Span start date(33)", "Occurrence Span end date (33)", "UB-82 locator 2", "UB-82 locator 9", "UB-82 locator 27", "UB-82 locator 45"};
		description = "UB82 Data";
		name = "UB1";
	}
}
