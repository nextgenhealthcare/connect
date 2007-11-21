package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCS extends Segment {
	public _PCS(){
		fields = new Class[]{_XON.class, _CX.class, _IS.class, _DR.class, _ST.class, _IS.class, _ST.class, _XON.class, _ST.class, _IS.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Informant", "External ID", "Sanction Type", "Dates", "License Number", "License Type", "State", "Detail Holder", "Comment", "Standing", "Revised By"};
		description = "Sanctions Information Segment";
		name = "PCS";
	}
}
