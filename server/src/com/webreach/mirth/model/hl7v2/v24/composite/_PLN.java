package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PLN extends Composite {
	public _PLN(){
		fields = new Class[]{_ST.class, _IS.class, _ST.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"ID Number (ST)", "Type of ID Number (IS)", "State/Other Qualifying Info", "Expiration Date"};
		description = "Practitioner ID Numbers";
		name = "PLN";
	}
}
