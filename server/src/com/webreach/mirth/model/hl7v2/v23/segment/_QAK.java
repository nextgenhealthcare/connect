package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QAK extends Segment {
	public _QAK(){
		fields = new Class[]{_ST.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Query Tag", "Query Response Status"};
		description = "Query Acknowledgement";
		name = "QAK";
	}
}
