package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _UB2 extends Segment {
	public _UB2(){
		fields = new Class[]{_SI.class, _ST.class, _IS.class, _ST.class, _ST.class, _UVC.class, _OCD.class, _OSP.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Co-insurance Days (9)", "Condition Code (24-30)", "Covered Days (7)", "Non-covered Days (8)", "Value Amount & Code", "Occurrence Code & Date (32-35)", "Occurrence Span Code/Dates (36)", "UB92 Locator 2 (state)", "UB92 Locator 11 (state)", "UB92 Locator 31 (national)", "Document Control Number", "UB92 Locator 49 (national)", "UB92 Locator 56 (state)", "UB92 Locator 57 (national)", "UB92 Locator 78 (state)", "Special Visit Count"};
		description = "UB92 Data";
		name = "UB2";
	}
}
