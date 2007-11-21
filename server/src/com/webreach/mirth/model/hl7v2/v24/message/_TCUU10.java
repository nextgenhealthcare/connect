package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _TCUU10 extends Message{	
	public _TCUU10(){
		segments = new Class[]{_MSH.class, _EQU.class, _TCC.class, _ROL.class};
		repeats = new int[]{0, 0, -1, 0};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{}; 
		description = "Automated Equipment Test Code Settings Update";
		name = "TCUU10";
	}
}
