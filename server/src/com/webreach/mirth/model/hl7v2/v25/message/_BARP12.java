package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BARP12 extends Message{	
	public _BARP12(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _PID.class, _PV1.class, _DG1.class, _DRG.class, _PR1.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0, -1};
		required = new boolean[]{true, false, true, true, true, false, false, true, false};
		groups = new int[][]{{8, 9, 0, 1}}; 
		description = "Update Diagnosis/Procedure";
		name = "BARP12";
	}
}
