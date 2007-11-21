package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SEC extends Segment {
	public _SEC(){
		fields = new Class[]{_PPN.class, _ST.class, _XON.class, _ST.class, _PPN.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false};
		fieldDescriptions = new String[]{"Login", "Password", "ClientID", "AccessLevel", "Revised By"};
		description = "Security Segment";
		name = "SEC";
	}
}
