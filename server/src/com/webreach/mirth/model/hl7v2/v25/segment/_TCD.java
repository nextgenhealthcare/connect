package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TCD extends Segment {
	public _TCD(){
		fields = new Class[]{_CE.class, _SN.class, _SN.class, _SN.class, _SN.class, _ID.class, _ID.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Universal Service Identifier", "Auto-dilution Factor", "Rerun Dilution Factor", "Pre-dilution Factor", "Endogenous Content of Pre-dilution Diluent", "Automatic Repeat Allowed", "Reflex Allowed", "Analyte Repeat Status"};
		description = "Test Code Detail";
		name = "TCD";
	}
}
