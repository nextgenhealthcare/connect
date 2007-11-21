package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORFR04 extends Message{	
	public _ORFR04(){
		segments = new Class[]{_MSH.class, _MSA.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, -1, 0};
		required = new boolean[]{true, true, true, false, true, false, false, true, false, false, false, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {10, 11, 1, 1}, {7, 12, 1, 1}, {5, 12, 1, 1}}; 
		description = "Observational Report (Response to Query For Results of Observation)";
		name = "ORFR04";
	}
}
