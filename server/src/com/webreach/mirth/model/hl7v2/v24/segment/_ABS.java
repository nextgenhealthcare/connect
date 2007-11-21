package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ABS extends Segment {
	public _ABS(){
		fields = new Class[]{_XCN.class, _CE.class, _CE.class, _TS.class, _XCN.class, _CE.class, _TS.class, _XCN.class, _CE.class, _ID.class, _CE.class, _NM.class, _CE.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Discharge Care Provider", "Transfer Medical Service Code", "Severity of Illness Code", "Date/Time of Attestation", "Attested By", "Triage Code", "Abstract Completion Date/Time", "Abstracted By", "Case Category Code", "Caesarian Section Indicator", "Gestation Category Code", "Gestation Period - Weeks", "Newborn Code", "Stillborn Indicator"};
		description = "Abstract";
		name = "ABS";
	}
}
