package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MDMT10 extends Message{	
	public _MDMT10(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PV1.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _NTE.class, _TXA.class, _OBX.class, _NTE.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1};
		required = new boolean[]{true, false, true, true, true, true, true, false, true, false, true, true, false};
		groups = new int[][]{{7, 8, 0, 1}, {6, 10, 0, 1}, {12, 13, 1, 1}}; 
		description = "Document Replacement Notification and Content";
		name = "MDMT10";
	}
}
