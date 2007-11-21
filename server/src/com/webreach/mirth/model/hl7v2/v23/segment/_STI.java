package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _STI extends Segment {
	public _STI(){
		fields = new Class[]{_XON.class, _CX.class, _XON.class, _DR.class, _XAD.class, _XTN.class, _XTN.class, _XTN.class, _ST.class, _IS.class, _IS.class, _PPN.class, _IS.class, _IS.class};
		repeats = new int[]{-1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Subject ID (internal)", "Definition ID (internal)", "Location ID (Internal)", "Dates", "Address", "Phone", "Fax", "Email", "Comment", "Standing ID", "Complete ID", "Revised By", "TransCode", "Performed By"};
		description = "Study Information Segment";
		name = "STI";
	}
}
