package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QCNJ01 extends Message{	
	public _QCNJ01(){
		segments = new Class[]{_MSH.class, _QID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{true, true};
		groups = new int[][]{}; 
		description = "Cancel Query/Acknowledge Message";
		name = "QCNJ01";
	}
}
