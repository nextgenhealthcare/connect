package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _DOCT12 extends Message{	
	public _DOCT12(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QRD.class, _EVN.class, _PID.class, _PV1.class, _TXA.class, _OBX.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, 0};
		required = new boolean[]{true, true, false, true, false, true, true, true, false, false};
		groups = new int[][]{{5, 9, 1, 1}}; 
		description = "Document Response";
		name = "DOCT12";
	}
}
