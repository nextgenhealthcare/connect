package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MDMT01 extends Message{	
	public _MDMT01(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _TXA.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, true, true};
		groups = new int[][]{}; 
		description = "Original Document Notification";
		name = "MDMT01";
	}
}
