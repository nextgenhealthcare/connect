package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRI extends Segment {
	public _QRI(){
		fields = new Class[]{_NM.class, _IS.class, _CE.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Candidate Confidence", "Match Reason Code", "Algorithm Descriptor"};
		description = "Query Response Instance";
		name = "QRI";
	}
}
