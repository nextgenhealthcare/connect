package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM2 extends Segment {
	public _OM2(){
		fields = new Class[]{_NM.class, _CE.class, _NM.class, _CE.class, _TX.class, _CM.class, _CM.class, _CM.class, _CM.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/ Observation Master File", "Units of Measure", "Range of Decimal Precision", "Corresponding SI Units of Measure", "SI Conversion Factor", "Reference (Normal) Range - Ordinal & Continuous Observation", "Critical Range for Ordinal & Continuous Observation", "Absolute Range for Ordinal & Continuous Observation", "Delta Check Criteria", "Minimum Meaningful Increments"};
		description = "Numeric Observation";
		name = "OM2";
	}
}
