package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BLC extends Segment {
	public _BLC(){
		fields = new Class[]{_CE.class, _CQ.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Blood Product Code", "Blood Amount"};
		description = "Blood Code";
		name = "BLC";
	}
}
