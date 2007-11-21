package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TCUU11 extends Message{	
	public _TCUU11(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _SPM.class, _TCC.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, -1, 0};
		required = new boolean[]{true, false, true, false, true, false};
		groups = new int[][]{{4, 5, 1, 1}}; 
		description = "Automated Equipment Test Code Settings Request";
		name = "TCUU11";
	}
}
