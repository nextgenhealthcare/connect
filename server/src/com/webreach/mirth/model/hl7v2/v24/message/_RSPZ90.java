package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPZ90 extends Message{	
	public _RSPZ90(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _RCP.class, _PID.class, _PD1.class, _NK1.class, _NTE.class, _PV1.class, _PV2.class, _ORC.class, _OBR.class, _NTE.class, _CTD.class, _OBX.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0};
		required = new boolean[]{true, true, false, true, true, true, true, false, false, false, true, false, true, true, false, false, false, false, true};
		groups = new int[][]{{11, 12, 0, 0}, {7, 12, 0, 0}, {17, 18, 1, 1}, {13, 18, 1, 1}, {7, 18, 1, 1}}; 
		description = "Lab Results History (response)";
		name = "RSPZ90";
	}
}
