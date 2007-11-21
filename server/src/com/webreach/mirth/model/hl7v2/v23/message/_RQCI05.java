package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQCI05 extends Message{	
	public _RQCI05(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _NTE.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, -1, -1, -1};
		required = new boolean[]{true, true, false, true, false, true, false, false, false};
		groups = new int[][]{{4, 5, 1, 1}}; 
		description = "Request For Patient Clinical Information";
		name = "RQCI05";
	}
}
