package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ESUU01 extends Message{	
	public _ESUU01(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _ISD.class, _ROL.class};
		repeats = new int[]{0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, false};
		groups = new int[][]{}; 
		description = "Automated Equipment Status Update";
		name = "ESUU01";
	}
}
