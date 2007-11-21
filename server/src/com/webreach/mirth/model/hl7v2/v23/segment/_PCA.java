package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCA extends Segment {
	public _PCA(){
		fields = new Class[]{_XON.class, _CX.class, _XAD.class, _XTN.class, _XTN.class, _PPN.class, _ST.class, _IS.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Affiliation", "External ID", "Address", "Phone", "Fax", "Revised By", "Comment", "Standing", "Date Joined"};
		description = "Affiliation Information Segment";
		name = "PCA";
	}
}
