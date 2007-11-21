package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM2 extends Segment {
	public _OM2(){
		fields = new Class[]{_NM.class, _CE.class, _NM.class, _CE.class, _TX.class, _RFR.class, _RFR.class, _RFR.class, _DLT.class, _NM.class};
		repeats = new int[]{0, 0, -1, 0, 0, -1, -1, 0, -1, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/Observation Master File", "Units of Measure", "Range of Decimal Precision", "Corresponding Si Units of Measure", "Si Conversion Factor", "Reference (normal) Range - Ordinal and Continuous Observations", "Critical Range For Ordinal and Continuous Observations", "Absolute Range For Ordinal and Continuous Observations", "Delta Check Criteria", "Minimum Meaningful Increments"};
		description = "Numeric Observation";
		name = "OM2";
	}
}
