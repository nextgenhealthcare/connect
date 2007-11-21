package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OM2 extends Segment {
	public _OM2(){
		fields = new Class[]{_NM.class, _CE.class, _NM.class, _CE.class, _TX.class, _RFR.class, _NR.class, _RFR.class, _DLT.class, _NM.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sequence Number - Test/Observation Master File", "Units of Measure", "Range of Decimal Precision", "Corresponding Si Units of Measure", "Si Conversion Factor", "Reference (normal) Range - Ordinal & Continuous OBS", "Critical Range For Ordinal & Continuous OBS", "Absolute Range For Ordinal & Continuous OBS", "Delta Check Criteria", "Minimum Meaningful Increments"};
		description = "Numeric Observation Segment";
		name = "OM2";
	}
}
