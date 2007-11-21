package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SSUU03 extends Message{	
	public _SSUU03(){
		segments = new Class[]{_MSH.class, _EQU.class, _SAC.class, _OBX.class, _ROL.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, false};
		groups = new int[][]{{3, 4, 1, 1}}; 
		description = "Specimen Status Update";
		name = "SSUU03";
	}
}
