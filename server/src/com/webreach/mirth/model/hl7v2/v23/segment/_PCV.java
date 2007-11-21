package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCV extends Segment {
	public _PCV(){
		fields = new Class[]{_DT.class, _ST.class, _DT.class, _IS.class, _ST.class, _IS.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Verified Date", "Verified By", "Expiration Date", "Verification Medium", "Comment", "Verification Standing", "Revised By"};
		description = "Verification Information Segment";
		name = "PCV";
	}
}
