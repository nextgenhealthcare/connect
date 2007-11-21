package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _OSRQ06 extends Message{	
	public _OSRQ06(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, 0, -1, 0, -1, -1, 0};
		required = new boolean[]{true, true, false, false, true, false, true, false, true, false, false, false};
		groups = new int[][]{{7, 8, 0, 0}, {9, 11, 1, 1}, {7, 11, 0, 0}}; 
		description = "Query For Order Status - Response";
		name = "OSRQ06";
	}
}
