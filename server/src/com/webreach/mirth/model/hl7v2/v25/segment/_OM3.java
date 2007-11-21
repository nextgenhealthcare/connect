package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM3 extends Segment {
	public _OM3(){
		fields = new Class[]{_NM.class, _CE.class, _CE.class, _CE.class, _CE.class, _CE.class, _ID.class};
		repeats = new int[]{0, 0, 0, -1, -1, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/Observation Master File", "Preferred Coding System", "Valid Coded 'answers'", "Normal Text/Codes For Categorical Observations", "Abnormal Text/Codes For Categorical Observations", "Critical Text/Codes For Categorical Observations", "Value Type"};
		description = "Categorical Service/Test/Observation";
		name = "OM3";
	}
}
