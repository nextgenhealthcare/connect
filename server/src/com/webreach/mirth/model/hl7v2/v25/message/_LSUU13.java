package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _LSUU13 extends Message{	
	public _LSUU13(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _EQP.class, _ROL.class};
		repeats = new int[]{0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, true, false};
		groups = new int[][]{}; 
		description = "Automated Equipment Log/Service Request";
		name = "LSUU13";
	}
}
