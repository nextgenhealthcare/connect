package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQCI06 extends Message{	
	public _RQCI06(){
		segments = new Class[]{_MSH.class, _QRD.class, _QRF.class, _PRD.class, _CTD.class, _PID.class, _NK1.class, _GT1.class, _NTE.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, -1, -1, -1};
		required = new boolean[]{true, true, false, true, false, true, false, false, false};
		groups = new int[][]{{4, 5, 1, 1}}; 
		description = "Request/Receipt of Clinical Data Listing";
		name = "RQCI06";
	}
}
