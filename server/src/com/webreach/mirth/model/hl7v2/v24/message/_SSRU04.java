package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _SSRU04 extends Message{	
	public _SSRU04(){
		segments = new Class[]{_MSH.class, _EQU.class, _SAC.class, _ROL.class};
		repeats = new int[]{0, 0, -1, 0};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{}; 
		description = "Specimen Status Request";
		name = "SSRU04";
	}
}
