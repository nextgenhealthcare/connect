package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _EACU07 extends Message{	
	public _EACU07(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _ECD.class, _TQ1.class, _SAC.class, _SPM.class, _CNS.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1, 0, 0};
		required = new boolean[]{true, false, true, true, false, true, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {4, 8, 1, 1}}; 
		description = "Automated Equipment Command";
		name = "EACU07";
	}
}
