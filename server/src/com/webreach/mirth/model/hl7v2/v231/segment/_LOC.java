package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LOC extends Segment {
	public _LOC(){
		fields = new Class[]{_PL.class, _ST.class, _IS.class, _XON.class, _XAD.class, _XTN.class, _CE.class, _IS.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Primary Key Value", "Location Description", "Location Type", "Organization Name", "Location Address", "Location Phone", "License Number", "Location Equipment"};
		description = "Location Identification";
		name = "LOC";
	}
}
