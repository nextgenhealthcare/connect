package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EACU07 extends Message{	
	public _EACU07(){
		segments = new Class[]{_MSH.class, _EQU.class, _ECD.class, _SAC.class, _CNS.class, _ROL.class};
		repeats = new int[]{0, 0, -1, 0, 0, 0};
		required = new boolean[]{true, true, true, false, false, false};
		groups = new int[][]{}; 
		description = "Automated Equipment Command";
		name = "EACU07";
	}
}
