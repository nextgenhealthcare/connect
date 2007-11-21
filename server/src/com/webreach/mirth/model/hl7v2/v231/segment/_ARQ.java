package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ARQ extends Segment {
	public _ARQ(){
		fields = new Class[]{_EI.class, _EI.class, _NM.class, _EI.class, _CE.class, _CE.class, _CE.class, _CE.class, _NM.class, _CE.class, _DR.class, _ST.class, _RI.class, _ST.class, _XCN.class, _XTN.class, _XAD.class, _PL.class, _XCN.class, _XTN.class, _PL.class, _EI.class, _EI.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Placer Appointment ID", "Filler Appointment ID", "Occurrence Number", "Placer Group Number", "Schedule ID", "Request Event Reason", "Appointment Reason", "Appointment Type", "Appointment Duration", "Appointment Duration Units", "Requested Start Date/Time Range", "Priority", "Repeating Interval", "Repeating Interval Duration", "Placer Contact Person", "Placer Contact Phone Number", "Placer Contact Address", "Placer Contact Location", "Entered by Person", "Entered by Phone Number", "Entered by Location", "Parent Placer Appointment ID", "Parent Filler Appointment ID"};
		description = "Appointment Request";
		name = "ARQ";
	}
}
