package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _UB1 extends Segment {
	public _UB1(){
		fields = new Class[]{_SI.class, _NM.class, _NM.class, _NM.class, _NM.class, _NM.class, _IS.class, _NM.class, _NM.class, _CM.class, _NM.class, _CE.class, _CE.class, _DT.class, _DT.class, _CM.class, _CE.class, _DT.class, _DT.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 5, 0, 0, 8, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Blood Deductible  (43)", "Blood Furnished-Pints of (40)", "Blood Replaced-Pints (41)", "Blood not Replaced-Pints(42)", "Co-Insurance Days (25)", "Condition Code (35-39)", "Covered Days   (23)", "Non Covered Days   (24)", "Value Amount & Code (46-49)", "Number of Grace Days (90)", "Spec Program Indicator (44)", "PSRO/UR Approval Indicator (87)", "PSRO/UR Approved Stay-Fm (88)", "PSRO/UR Approved Stay-To (89)", "Occurrence (28-32)", "Occurrence Span (33)", "Occur Span Start Date(33)", "Occur Span End Date (33)", "UB-82 Locator 2", "UB-82 Locator 9", "UB-82 Locator 27", "UB-82 Locator 45"};
		description = "UB82 Data";
		name = "UB1";
	}
}
