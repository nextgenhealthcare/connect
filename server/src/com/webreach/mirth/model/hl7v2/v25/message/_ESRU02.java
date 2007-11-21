package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ESRU02 extends Message{	
	public _ESRU02(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0};
		required = new boolean[]{true, false, true, false};
		groups = new int[][]{}; 
		description = "Automated Equipment Status Request";
		name = "ESRU02";
	}
}
