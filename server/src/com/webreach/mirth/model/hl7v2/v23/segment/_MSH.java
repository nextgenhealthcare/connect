package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MSH extends Segment {
	public _MSH(){
		fields = new Class[]{_ST.class, _ST.class, _EI.class, _EI.class, _EI.class, _EI.class, _TS.class, _ST.class, _CM_MSH.class, _ST.class, _PT.class, _ID.class, _NM.class, _ST.class, _ID.class, _ID.class, _ID.class, _ID.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Field Separator", "Encoding Characters", "Sending Application", "Sending Facility", "Receiving Application", "Receiving Facility", "Date/Time of Message", "Security", "Message Type", "Message Control ID", "Processing ID", "Version ID", "Sequence Number", "Continuation Pointer", "Accept Acknowledgement Type", "Application Acknowledgement Type", "Country Code", "Character Set", "Principal Language of Message"};
		description = "Message Header";
		name = "MSH";
	}
}
