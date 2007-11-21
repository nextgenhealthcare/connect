package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MDMT04 extends Message{	
	public _MDMT04(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _TXA.class, _OBX.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, true, true};
		groups = new int[][]{}; 
		description = "Document Status Change Notification and Content";
		name = "MDMT04";
	}
}
