package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BTS extends Segment {
	public _BTS(){
		fields = new Class[]{_ST.class, _ST.class, _NM.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Batch Message Count", "Batch Comment", "Batch Totals"};
		description = "Batch Trailer";
		name = "BTS";
	}
}
