package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TXA extends Segment {
	public _TXA(){
		fields = new Class[]{_SI.class, _IS.class, _ID.class, _TS.class, _XCN.class, _TS.class, _TS.class, _TS.class, _XCN.class, _XCN.class, _XCN.class, _EI.class, _EI.class, _EI.class, _EI.class, _ST.class, _ID.class, _ID.class, _ID.class, _ID.class, _ST.class, _PPN.class, _XCN.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, -1, -1, -1, -1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set Id- Txa", "Document Type", "Document Content Presentation", "Activity Date/Time", "Primary Activity Provider Code/Name", "Origination Date/Time", "Transcription Date/Time", "Edit Date/Time", "Originator Code/Name", "Assigned Document Authenticator", "Transcriptionist Code/Name", "Unique Document Number", "Parent Document Number", "Placer Order Number", "Filler Order Number", "Unique Document File Name", "Document Completion Status", "Document Confidentiality Status", "Document Availability Status", "Document Storage Status", "Document Change Reason", "Authentication Person, Time Stamp", "Distributed Copies (code and Name of Recipients)"};
		description = "Transcription Document Header";
		name = "TXA";
	}
}
