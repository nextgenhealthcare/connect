package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EARU08 extends Message{	
	public _EARU08(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _ECD.class, _SAC.class, _SPM.class, _ECR.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1, 0, 0};
		required = new boolean[]{true, false, true, true, true, false, true, false};
		groups = new int[][]{{5, 6, 0, 0}, {4, 7, 1, 1}}; 
		description = "Automated Equipment Response";
		name = "EARU08";
	}
}
