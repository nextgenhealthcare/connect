package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PLN extends Composite {
	public _PLN(){
		fields = new Class[]{_ST.class, _IS.class, _ST.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"ID Number", "Type of ID Number", "State/Other Qualifying Information", "Expiration Date"};
		description = "Practitioner License or Other ID Number";
		name = "PLN";
	}
}
