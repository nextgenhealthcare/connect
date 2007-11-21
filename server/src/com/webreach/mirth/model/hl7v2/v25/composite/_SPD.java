package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SPD extends Composite {
	public _SPD(){
		fields = new Class[]{_ST.class, _ST.class, _ID.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Specialty Name", "Governing Board", "Eligible or Certified", "Date of Certification"};
		description = "Specialty Description";
		name = "SPD";
	}
}
