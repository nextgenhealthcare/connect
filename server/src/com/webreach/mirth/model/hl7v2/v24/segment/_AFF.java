package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _AFF extends Segment {
	public _AFF(){
		fields = new Class[]{_SI.class, _XON.class, _XAD.class, _DR.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Professional Organization", "Professional Organization Address", "Professional Organization Affiliation Date Range", "Professional Affiliation Additional Information"};
		description = "Professional Affiliation";
		name = "AFF";
	}
}
