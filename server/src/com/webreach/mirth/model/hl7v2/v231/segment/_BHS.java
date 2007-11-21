package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BHS extends Segment {
	public _BHS(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _TS.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Batch Field Separator", "Batch Encoding Characters", "Batch Sending Application", "Batch Sending Facility", "Batch Receiving Application", "Batch Receiving Facility", "Batch Creation Date/Time", "Batch Security", "Batch Name/Id/Type", "Batch Comment", "Batch Control ID", "Reference Batch Control ID"};
		description = "Batch Header";
		name = "BHS";
	}
}
