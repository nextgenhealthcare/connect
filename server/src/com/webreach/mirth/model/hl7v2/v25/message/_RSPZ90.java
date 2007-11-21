package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPZ90 extends Message{	
	public _RSPZ90(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _RCP.class, _PID.class, _PD1.class, _NK1.class, _NTE.class, _PV1.class, _PV2.class, _ORC.class, _TQ1.class, _TQ2.class, _OBR.class, _NTE.class, _CTD.class, _OBX.class, _NTE.class, _SPM.class, _OBX.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, true, true, true, true, false, false, false, true, false, true, true, false, true, false, false, false, false, true, false, true};
		groups = new int[][]{{12, 13, 0, 0}, {8, 13, 0, 0}, {15, 16, 0, 1}, {20, 21, 1, 1}, {14, 21, 1, 1}, {22, 23, 0, 1}, {8, 23, 1, 1}}; 
		description = "Lab Results History (response)";
		name = "RSPZ90";
	}
}
