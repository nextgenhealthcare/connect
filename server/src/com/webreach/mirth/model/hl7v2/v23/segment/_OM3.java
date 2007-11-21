package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM3 extends Segment {
	public _OM3(){
		fields = new Class[]{_NM.class, _CE.class, _CE.class, _CE.class, _CE.class, _CE.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/ Observation Master File", "Preferred Coding System", "Valid Coded 'Answers'", "Normal Text/Codes for Categorical Observations", "Abnormal Text/Codes for Categorical Observations", "Critical Text Codes for Categorical Observations", "Value Type"};
		description = "Categorical Test/Observation";
		name = "OM3";
	}
}
