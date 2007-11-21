package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MDMT05 extends Message{	
	public _MDMT05(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PV1.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _NTE.class, _TXA.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, true, true, true, true, false, true, false, true};
		groups = new int[][]{{7, 8, 0, 1}, {6, 10, 0, 1}}; 
		description = "Document Addendum Notification";
		name = "MDMT05";
	}
}
