package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _RFR extends Composite {
	public _RFR(){
		fields = new Class[]{_NR.class, _IS.class, _NR.class, _NR.class, _TX.class, _ST.class, _TX.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Reference Range", "Administrative Sex", "Age Range", "Gestational Range", "Species", "Race/Subspecies", "Conditions"};
		description = "Reference Range";
		name = "RFR";
	}
}
