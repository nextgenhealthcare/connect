package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QCNJ01 extends Message{	
	public _QCNJ01(){
		segments = new Class[]{_MSH.class, _SFT.class, _QID.class};
		repeats = new int[]{0, -1, 0};
		required = new boolean[]{true, false, true};
		groups = new int[][]{}; 
		description = "Cancel Query/Acknowledge Message";
		name = "QCNJ01";
	}
}
