package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SSRU04 extends Message{	
	public _SSRU04(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _SAC.class, _SPM.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0};
		required = new boolean[]{true, false, true, true, false, false};
		groups = new int[][]{{4, 5, 1, 1}}; 
		description = "Specimen Status Request";
		name = "SSRU04";
	}
}
