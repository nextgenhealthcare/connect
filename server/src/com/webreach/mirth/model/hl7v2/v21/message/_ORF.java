package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORF extends Message{	
	public _ORF(){
		segments = new Class[]{_MSH.class, _MSA.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, true, true, false, false, false, false, true, false, false, false, false};
		groups = new int[][]{{10, 11, 1, 1}, {7, 11, 1, 1}, {3, 11, 1, 1}}; 
		description = "Observation Result/Record Response";
		name = "ORF";
	}
}
