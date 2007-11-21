package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RFR extends Composite {
	public _RFR(){
		fields = new Class[]{_NR.class, _IS.class, _NR.class, _NR.class, _ST.class, _ST.class, _TX.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Numeric Range", "Administrative Sex", "Age Range", "Gestational Age Range", "Species", "Race/Subspecies", "Conditions"};
		description = "Reference Range";
		name = "RFR";
	}
}
