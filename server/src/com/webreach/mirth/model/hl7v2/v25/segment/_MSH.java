package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MSH extends Segment {
	public _MSH(){
		fields = new Class[]{_ST.class, _ST.class, _HD.class, _HD.class, _HD.class, _HD.class, _TS.class, _ST.class, _MSG.class, _ST.class, _PT.class, _VID.class, _NM.class, _ST.class, _ID.class, _ID.class, _ID.class, _ID.class, _CE.class, _ID.class, _EI.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0, 0, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Field Separator", "Encoding Characters", "Sending Application", "Sending Facility", "Receiving Application", "Receiving Facility", "Date/Time of Message", "Security", "Message Type", "Message Control ID", "Processing ID", "Version ID", "Sequence Number", "Continuation Pointer", "Accept Acknowledgment Type", "Application Acknowledgment Type", "Country Code", "Character Set", "Principal Language of Message", "Alternate Character Set Handling Scheme", "Message Profile Identifier"};
		description = "Message Header";
		name = "MSH";
	}
}
