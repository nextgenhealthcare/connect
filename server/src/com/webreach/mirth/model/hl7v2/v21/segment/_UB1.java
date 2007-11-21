package com.webreach.mirth.model.hl7v2.v21.segment;
import com.webreach.mirth.model.hl7v2.v21.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _UB1 extends Segment {
	public _UB1(){
		fields = new Class[]{_SI.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class, _ST.class, _ST.class, _CM.class, _ST.class, _ID.class, _ID.class, _DT.class, _DT.class, _ID.class, _ID.class, _DT.class, _DT.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 5, 0, 0, 8, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Blood Deductible", "Blood Furn.-Pints of (40)", "Blook Replaced-Pints (41)", "Blood not Rplcd-Pints(42)", "Co-Insurance Days (25)", "Condition Code", "Covered Days (23)", "Non-Covered Days (24)", "Value Amount & Code", "Number of Grace Days (90)", "Spec. Prog. Indicator(44)", "Psro/Ur Approvl Ind. (87)", "Psro/Ur Aprvd Stay-Fm(88)", "Psro/Ur Aprvd Stay-To(89)", "Occurrence (28-32)", "Occurrence Span (33)", "Occur Span Start Date(33)", "Occur Span End Date (33)", "UB-82 Locator 2", "UB-82 Locator 9", "UB-82 Locator 27", "UB-82 Locator 45"};
		description = "UB82 Data";
		name = "UB1";
	}
}
