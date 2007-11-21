package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SCH extends Segment {
	public _SCH(){
		fields = new Class[]{_EI.class, _EI.class, _NM.class, _EI.class, _CE.class, _CE.class, _CE.class, _CE.class, _NM.class, _CE.class, _TQ.class, _XCN.class, _XTN.class, _XAD.class, _PL.class, _XCN.class, _XTN.class, _XAD.class, _PL.class, _XCN.class, _XTN.class, _PL.class, _EI.class, _EI.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Placer Appointment ID", "Filler Appointment ID", "Occurrence Number", "Placer Group Number", "Schedule ID", "Event Reason", "Appointment Reason", "Appointment Type", "Appointment Duration", "Appointment Duration Units", "Appointment Timing Quantity", "Placer Contact Person", "Placer Contact Phone Number", "Placer Contact Address", "Placer Contact Location", "Filler Contact Person", "Filler Contact Phone Number", "Filler Contact Address", "Filler Contact Location", "Entered by Person", "Entered by Phone Number", "Entered by Location", "Parent Placer Appointment ID", "Parent Filler Appointment ID", "Filler Status Code"};
		description = "Schedule Activity Information";
		name = "SCH";
	}
}
