package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TCC extends Segment {
	public _TCC(){
		fields = new Class[]{_CE.class, _EI.class, _SPS.class, _SN.class, _SN.class, _SN.class, _SN.class, _NM.class, _ID.class, _ID.class, _ID.class, _SN.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Universal Service Identifier", "Test Application Identifier", "Specimen Source", "Auto-dilution Factor Default", "Rerun Dilution Factor Default", "Pre-dilution Factor Default", "Endogenous Content of Pre-dilution Diluent", "Inventory Limits Warning Level", "Automatic Rerun Allowed", "Automatic Repeat Allowed", "Automatic Reflex Allowed", "Equipment Dynamic Range", "Units", "Processing Type"};
		description = "Test Code Configuration";
		name = "TCC";
	}
}
