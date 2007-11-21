package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCL extends Segment {
	public _PCL(){
		fields = new Class[]{_XON.class, _CX.class, _ST.class, _IS.class, _ST.class, _DT.class, _ST.class, _ST.class, _ST.class, _ST.class, _DT.class, _DT.class, _ST.class, _IS.class, _ST.class, _PPN.class, _ST.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"License Issuer", "External ID", "License Number", "License Type", "State", "Expiration Date", "In Force", "Is Original", "Supervision Required", "Practice Under Other Provider", "Initial License Date", "Current License Date", "Is Restricted", "Drug Schedule", "UPIN", "Revised By", "Comment", "Standing"};
		description = "License Information Segment";
		name = "PCL";
	}
}
