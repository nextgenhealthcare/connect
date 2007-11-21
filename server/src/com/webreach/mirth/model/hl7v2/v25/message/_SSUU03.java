package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SSUU03 extends Message{	
	public _SSUU03(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _SAC.class, _OBX.class, _SPM.class, _OBX.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, true, false, true, false, false};
		groups = new int[][]{{6, 7, 0, 1}, {4, 7, 1, 1}}; 
		description = "Specimen Status Update";
		name = "SSUU03";
	}
}
