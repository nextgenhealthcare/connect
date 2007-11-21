package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PDA extends Segment {
	public _PDA(){
		fields = new Class[]{_CE.class, _PL.class, _ID.class, _TS.class, _XCN.class, _ID.class, _DR.class, _XCN.class, _ID.class};
		repeats = new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Death Cause Code", "Death Location", "Death Certified Indicator", "Death Certificate Signed Date/Time", "Death Certified By", "Autopsy Indicator", "Autopsy Start and End Date/Time", "Autopsy Performed By", "Coroner Indicator"};
		description = "Patient Death and Autopsy";
		name = "PDA";
	}
}
