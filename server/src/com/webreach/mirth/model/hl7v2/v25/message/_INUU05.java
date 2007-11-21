package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _INUU05 extends Message{	
	public _INUU05(){
		segments = new Class[]{_MSH.class, _SFT.class, _EQU.class, _INV.class, _ROL.class};
		repeats = new int[]{0, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, true, false};
		groups = new int[][]{}; 
		description = "Automated Equipment Inventory Update";
		name = "INUU05";
	}
}
