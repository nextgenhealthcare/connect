package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PES extends Segment {
	public _PES(){
		fields = new Class[]{_XON.class, _XCN.class, _XAD.class, _XTN.class, _EI.class, _NM.class, _FT.class, _FT.class, _TS.class, _TS.class, _ID.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Sender Organization Name", "Sender Individual Name", "Sender Address", "Sender Telephone", "Sender Event Identifier", "Sender Sequence Number", "Sender Event Description", "Sender Comment", "Sender Aware Date/Time", "Event Report Date", "Event Report Timing/Type", "Event Report Source", "Event Reported To"};
		description = "Product Experience Sender";
		name = "PES";
	}
}
